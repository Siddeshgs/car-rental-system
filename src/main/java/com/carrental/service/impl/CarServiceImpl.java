package com.carrental.service.impl;

import com.carrental.dto.CarDto;
import com.carrental.entity.Car;
import com.carrental.enums.CarCategory;
import com.carrental.enums.CarStatus;
import com.carrental.enums.FuelType;
import com.carrental.enums.Transmission;
import com.carrental.exception.ResourceNotFoundException;
import com.carrental.repository.CarRepository;
import com.carrental.service.CarService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;

    public CarServiceImpl(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Car getCarById(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with ID: " + id));
    }

    @Override
    public Car createCar(CarDto carDto) {
        if (carRepository.existsByRegistrationNumber(carDto.getRegistrationNumber())) {
            throw new IllegalArgumentException("A car with registration number '" + carDto.getRegistrationNumber() + "' already exists.");
        }

        Car car = new Car(
                carDto.getMake(),
                carDto.getModel(),
                carDto.getYear(),
                carDto.getCategory(),
                carDto.getDailyRate(),
                carDto.getFuelType(),
                carDto.getTransmission(),
                carDto.getSeats(),
                carDto.getRegistrationNumber(),
                carDto.getMileage(),
                carDto.getStatus() != null ? carDto.getStatus() : CarStatus.AVAILABLE,
                carDto.getImageUrl(),
                carDto.getFeatures()
        );

        return carRepository.save(car);
    }

    @Override
    public Car updateCar(Long id, CarDto carDto) {
        Car car = getCarById(id);

        if (!car.getRegistrationNumber().equalsIgnoreCase(carDto.getRegistrationNumber())
                && carRepository.existsByRegistrationNumber(carDto.getRegistrationNumber())) {
            throw new IllegalArgumentException("Registration number '" + carDto.getRegistrationNumber() + "' is already in use by another car.");
        }

        car.setMake(carDto.getMake());
        car.setModel(carDto.getModel());
        car.setYear(carDto.getYear());
        car.setCategory(carDto.getCategory());
        car.setDailyRate(carDto.getDailyRate());
        car.setFuelType(carDto.getFuelType());
        car.setTransmission(carDto.getTransmission());
        car.setSeats(carDto.getSeats());
        car.setRegistrationNumber(carDto.getRegistrationNumber());
        car.setMileage(carDto.getMileage());
        if (carDto.getStatus() != null) {
            car.setStatus(carDto.getStatus());
        }
        car.setImageUrl(carDto.getImageUrl());
        car.setFeatures(carDto.getFeatures());

        return carRepository.save(car);
    }

    @Override
    public void deleteCar(Long id) {
        Car car = getCarById(id);
        carRepository.delete(car);
    }

    @Override
    public Car updateCarStatus(Long id, CarStatus status) {
        Car car = getCarById(id);
        car.setStatus(status);
        return carRepository.save(car);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Car> getAvailableCars() {
        return carRepository.findByStatus(CarStatus.AVAILABLE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Car> filterCars(CarCategory category, CarStatus status, FuelType fuelType,
                                Transmission transmission, BigDecimal maxRate, String search) {
        String searchQuery = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        return carRepository.filterCars(category, status, fuelType, transmission, maxRate, searchQuery);
    }

    @Override
    public CarDto mapToDto(Car car) {
        CarDto dto = new CarDto();
        dto.setId(car.getId());
        dto.setMake(car.getMake());
        dto.setModel(car.getModel());
        dto.setYear(car.getYear());
        dto.setCategory(car.getCategory());
        dto.setDailyRate(car.getDailyRate());
        dto.setFuelType(car.getFuelType());
        dto.setTransmission(car.getTransmission());
        dto.setSeats(car.getSeats());
        dto.setRegistrationNumber(car.getRegistrationNumber());
        dto.setMileage(car.getMileage());
        dto.setStatus(car.getStatus());
        dto.setImageUrl(car.getImageUrl());
        dto.setFeatures(car.getFeatures());
        return dto;
    }
}
