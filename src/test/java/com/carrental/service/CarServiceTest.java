package com.carrental.service;

import com.carrental.dto.CarDto;
import com.carrental.entity.Car;
import com.carrental.enums.CarCategory;
import com.carrental.enums.CarStatus;
import com.carrental.enums.FuelType;
import com.carrental.enums.Transmission;
import com.carrental.exception.ResourceNotFoundException;
import com.carrental.repository.CarRepository;
import com.carrental.service.impl.CarServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private CarServiceImpl carService;

    private Car sampleCar;
    private CarDto sampleDto;

    @BeforeEach
    void setUp() {
        sampleCar = new Car(
                "Audi", "Q7", 2023, CarCategory.SUV,
                new BigDecimal("120.00"), FuelType.PETROL, Transmission.AUTOMATIC,
                7, "AUDI-999", 15000, CarStatus.AVAILABLE,
                "http://example.com/audi.jpg", "Leather, Sunroof"
        );
        sampleCar.setId(1L);

        sampleDto = new CarDto();
        sampleDto.setMake("Audi");
        sampleDto.setModel("Q7");
        sampleDto.setYear(2023);
        sampleDto.setCategory(CarCategory.SUV);
        sampleDto.setDailyRate(new BigDecimal("120.00"));
        sampleDto.setFuelType(FuelType.PETROL);
        sampleDto.setTransmission(Transmission.AUTOMATIC);
        sampleDto.setSeats(7);
        sampleDto.setRegistrationNumber("AUDI-999");
        sampleDto.setMileage(15000);
    }

    @Test
    @DisplayName("Should create car successfully")
    void testCreateCar_Success() {
        when(carRepository.existsByRegistrationNumber("AUDI-999")).thenReturn(false);
        when(carRepository.save(any(Car.class))).thenAnswer(inv -> {
            Car c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        Car created = carService.createCar(sampleDto);

        assertNotNull(created);
        assertEquals("Audi", created.getMake());
        assertEquals("AUDI-999", created.getRegistrationNumber());
        verify(carRepository, times(1)).save(any(Car.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException on duplicate registration number")
    void testCreateCar_DuplicateRegistration_ThrowsException() {
        when(carRepository.existsByRegistrationNumber("AUDI-999")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> carService.createCar(sampleDto));
        verify(carRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fetch car by ID")
    void testGetCarById_Success() {
        when(carRepository.findById(1L)).thenReturn(Optional.of(sampleCar));

        Car found = carService.getCarById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
        assertEquals("Audi", found.getMake());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when car not found")
    void testGetCarById_NotFound_ThrowsException() {
        when(carRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> carService.getCarById(99L));
    }

    @Test
    @DisplayName("Should update car status")
    void testUpdateCarStatus() {
        when(carRepository.findById(1L)).thenReturn(Optional.of(sampleCar));
        when(carRepository.save(any(Car.class))).thenAnswer(inv -> inv.getArgument(0));

        Car updated = carService.updateCarStatus(1L, CarStatus.MAINTENANCE);

        assertEquals(CarStatus.MAINTENANCE, updated.getStatus());
    }

    @Test
    @DisplayName("Should return available cars only")
    void testGetAvailableCars() {
        when(carRepository.findByStatus(CarStatus.AVAILABLE)).thenReturn(Arrays.asList(sampleCar));

        List<Car> available = carService.getAvailableCars();

        assertEquals(1, available.size());
        assertEquals(CarStatus.AVAILABLE, available.get(0).getStatus());
    }
}
