package com.carrental.service;

import com.carrental.dto.BookingRequest;
import com.carrental.dto.BookingResponse;
import com.carrental.dto.ReturnCarRequest;
import com.carrental.entity.Booking;
import com.carrental.entity.Car;
import com.carrental.entity.Customer;
import com.carrental.enums.*;
import com.carrental.exception.CarUnavailableException;
import com.carrental.exception.InvalidBookingException;
import com.carrental.exception.ResourceNotFoundException;
import com.carrental.repository.BookingRepository;
import com.carrental.repository.CarRepository;
import com.carrental.repository.CustomerRepository;
import com.carrental.repository.PaymentRepository;
import com.carrental.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Car sampleCar;
    private Customer sampleCustomer;
    private Booking sampleBooking;

    @BeforeEach
    void setUp() {
        sampleCar = new Car(
                "Tesla", "Model 3", 2024, CarCategory.ELECTRIC,
                new BigDecimal("100.00"), FuelType.ELECTRIC, Transmission.AUTOMATIC,
                5, "TSLA-001", 10000, CarStatus.AVAILABLE,
                "http://example.com/tesla.jpg", "Autopilot"
        );
        sampleCar.setId(1L);

        sampleCustomer = new Customer(
                "John", "Doe", "john.doe@example.com",
                "+1 555-0100", "DL-12345", "123 Main St"
        );
        sampleCustomer.setId(1L);

        sampleBooking = new Booking(
                "BK-TEST1234", sampleCar, sampleCustomer,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(4),
                3, new BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("300.00"), BookingStatus.CONFIRMED,
                "Main Branch", "Main Branch", "Test Notes"
        );
        sampleBooking.setId(1L);
    }

    @Test
    @DisplayName("Should successfully create a booking when car is available")
    void testCreateBooking_Success() {
        BookingRequest request = new BookingRequest();
        request.setCarId(1L);
        request.setCustomerId(1L);
        request.setStartDate(LocalDate.now().plusDays(2));
        request.setEndDate(LocalDate.now().plusDays(5));
        request.setIncludeInsurance(false);
        request.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        when(carRepository.findById(1L)).thenReturn(Optional.of(sampleCar));
        when(bookingRepository.findConflictingBookings(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(10L);
            return b;
        });

        BookingResponse response = bookingService.createBooking(request);

        assertNotNull(response);
        assertEquals("Tesla", response.getCarMake());
        assertEquals("John Doe", response.getCustomerName());
        assertEquals(3, response.getTotalDays());
        assertEquals(new BigDecimal("300.00"), response.getTotalAmount());
        assertEquals(BookingStatus.CONFIRMED, response.getStatus());

        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(paymentRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should calculate insurance and promo discount correctly")
    void testCreateBooking_WithInsuranceAndDiscount() {
        BookingRequest request = new BookingRequest();
        request.setCarId(1L);
        request.setCustomerId(1L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(4)); // 3 days @ $100 = $300 base
        request.setIncludeInsurance(true); // 3 * $15 = $45
        request.setPromoCode("SAVE10"); // 10% of 300 = $30 discount

        when(carRepository.findById(1L)).thenReturn(Optional.of(sampleCar));
        when(bookingRepository.findConflictingBookings(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.createBooking(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("45.00"), response.getInsuranceFee());
        assertEquals(new BigDecimal("30.00"), response.getDiscountAmount());
        // Total = 300 + 45 - 30 = 315.00
        assertEquals(new BigDecimal("315.00"), response.getTotalAmount());
    }

    @Test
    @DisplayName("Should throw CarUnavailableException when dates conflict")
    void testCreateBooking_DateConflict_ThrowsException() {
        BookingRequest request = new BookingRequest();
        request.setCarId(1L);
        request.setCustomerId(1L);
        request.setStartDate(LocalDate.now().plusDays(2));
        request.setEndDate(LocalDate.now().plusDays(5));

        when(carRepository.findById(1L)).thenReturn(Optional.of(sampleCar));
        when(bookingRepository.findConflictingBookings(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(sampleBooking));

        assertThrows(CarUnavailableException.class, () -> bookingService.createBooking(request));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw CarUnavailableException when car is in maintenance")
    void testCreateBooking_CarInMaintenance_ThrowsException() {
        sampleCar.setStatus(CarStatus.MAINTENANCE);

        BookingRequest request = new BookingRequest();
        request.setCarId(1L);
        request.setStartDate(LocalDate.now().plusDays(2));
        request.setEndDate(LocalDate.now().plusDays(5));

        when(carRepository.findById(1L)).thenReturn(Optional.of(sampleCar));

        assertThrows(CarUnavailableException.class, () -> bookingService.createBooking(request));
    }

    @Test
    @DisplayName("Should throw InvalidBookingException when start date is after end date")
    void testCreateBooking_InvalidDates_ThrowsException() {
        BookingRequest request = new BookingRequest();
        request.setStartDate(LocalDate.now().plusDays(5));
        request.setEndDate(LocalDate.now().plusDays(2));

        assertThrows(InvalidBookingException.class, () -> bookingService.createBooking(request));
    }

    @Test
    @DisplayName("Should start rental and update car status to RENTED")
    void testStartRental_UpdatesStatus() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(sampleBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.startRental(1L);

        assertEquals(BookingStatus.ACTIVE, response.getStatus());
        assertEquals(CarStatus.RENTED, sampleCar.getStatus());
        verify(carRepository, times(1)).save(sampleCar);
    }

    @Test
    @DisplayName("Should complete rental and release car back to AVAILABLE")
    void testCompleteRental_ReleasesCar() {
        sampleBooking.setStatus(BookingStatus.ACTIVE);
        sampleCar.setStatus(CarStatus.RENTED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(sampleBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        ReturnCarRequest returnReq = new ReturnCarRequest();
        returnReq.setReturnDate(sampleBooking.getEndDate());
        returnReq.setCurrentMileage(10500);

        BookingResponse response = bookingService.completeRental(1L, returnReq);

        assertEquals(BookingStatus.COMPLETED, response.getStatus());
        assertEquals(CarStatus.AVAILABLE, sampleCar.getStatus());
        assertEquals(10500, sampleCar.getMileage());
    }

    @Test
    @DisplayName("Should apply late fee penalty when car returned after scheduled end date")
    void testCompleteRental_WithLatePenalty() {
        sampleBooking.setStatus(BookingStatus.ACTIVE);
        sampleBooking.setEndDate(LocalDate.now().minusDays(2)); // Scheduled return was 2 days ago

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(sampleBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        ReturnCarRequest returnReq = new ReturnCarRequest();
        returnReq.setReturnDate(LocalDate.now()); // Returned today -> 2 days late

        BookingResponse response = bookingService.completeRental(1L, returnReq);

        // Daily late penalty = $100 * 1.5 = $150/day * 2 days = $300
        assertEquals(new BigDecimal("300.00"), response.getLateFee());
        assertEquals(new BigDecimal("600.00"), response.getTotalAmount()); // 300 original + 300 late fee
    }

    @Test
    @DisplayName("Should cancel booking and release car")
    void testCancelBooking_Success() {
        sampleBooking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(sampleBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.cancelBooking(1L, "Customer changed schedule");

        assertEquals(BookingStatus.CANCELLED, response.getStatus());
        assertTrue(sampleBooking.getNotes().contains("Customer changed schedule"));
    }
}
