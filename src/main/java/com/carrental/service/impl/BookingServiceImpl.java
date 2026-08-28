package com.carrental.service.impl;

import com.carrental.dto.BookingRequest;
import com.carrental.dto.BookingResponse;
import com.carrental.dto.ReturnCarRequest;
import com.carrental.entity.Booking;
import com.carrental.entity.Car;
import com.carrental.entity.Customer;
import com.carrental.entity.Payment;
import com.carrental.enums.BookingStatus;
import com.carrental.enums.CarStatus;
import com.carrental.enums.PaymentMethod;
import com.carrental.enums.PaymentStatus;
import com.carrental.exception.CarUnavailableException;
import com.carrental.exception.InvalidBookingException;
import com.carrental.exception.ResourceNotFoundException;
import com.carrental.repository.BookingRepository;
import com.carrental.repository.CarRepository;
import com.carrental.repository.CustomerRepository;
import com.carrental.repository.PaymentRepository;
import com.carrental.service.BookingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private static final BigDecimal DAILY_INSURANCE_RATE = new BigDecimal("15.00");
    private static final BigDecimal LATE_RETURN_DAILY_SURCHARGE = new BigDecimal("1.50"); // 1.5x daily rate penalty

    private final BookingRepository bookingRepository;
    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              CarRepository carRepository,
                              CustomerRepository customerRepository,
                              PaymentRepository paymentRepository) {
        this.bookingRepository = bookingRepository;
        this.carRepository = carRepository;
        this.customerRepository = customerRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + id));
        return mapToResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingByReference(String reference) {
        Booking booking = bookingRepository.findByBookingReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with reference: " + reference));
        return mapToResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByCustomer(Long customerId) {
        return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponse createBooking(BookingRequest request) {
        // 1. Validate dates
        validateBookingDates(request.getStartDate(), request.getEndDate());

        // 2. Fetch car and check availability
        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with ID: " + request.getCarId()));

        if (car.getStatus() == CarStatus.MAINTENANCE) {
            throw new CarUnavailableException("The selected car (" + car.getMake() + " " + car.getModel() + ") is currently undergoing maintenance.");
        }

        if (!isCarAvailable(car.getId(), request.getStartDate(), request.getEndDate(), null)) {
            throw new CarUnavailableException("The selected car is already booked for the specified dates.");
        }

        // 3. Resolve Customer (fetch existing or create new)
        Customer customer = resolveCustomer(request);

        // 4. Calculate pricing
        long daysBetween = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        int totalDays = (int) (daysBetween == 0 ? 1 : daysBetween); // Minimum 1 day

        BigDecimal dailyRate = car.getDailyRate();
        BigDecimal baseTotal = dailyRate.multiply(BigDecimal.valueOf(totalDays));
        
        BigDecimal insuranceFee = BigDecimal.ZERO;
        if (request.isIncludeInsurance()) {
            insuranceFee = DAILY_INSURANCE_RATE.multiply(BigDecimal.valueOf(totalDays));
        }

        BigDecimal discountAmount = calculateDiscount(request.getPromoCode(), baseTotal, totalDays);
        BigDecimal totalAmount = baseTotal.add(insuranceFee).subtract(discountAmount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        // 5. Generate unique booking reference
        String bookingRef = "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Booking booking = new Booking(
                bookingRef,
                car,
                customer,
                request.getStartDate(),
                request.getEndDate(),
                totalDays,
                dailyRate,
                insuranceFee,
                discountAmount,
                totalAmount,
                BookingStatus.CONFIRMED,
                request.getPickupLocation() != null ? request.getPickupLocation() : "Main City Branch",
                request.getDropoffLocation() != null ? request.getDropoffLocation() : "Main City Branch",
                request.getNotes()
        );

        Booking savedBooking = bookingRepository.save(booking);

        // 6. Record payment
        Payment payment = new Payment(
                savedBooking,
                totalAmount,
                request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.CREDIT_CARD,
                PaymentStatus.PAID,
                "TXN-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase(),
                LocalDateTime.now()
        );
        paymentRepository.save(payment);

        return mapToResponse(savedBooking);
    }

    @Override
    public BookingResponse confirmBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + id));

        booking.setStatus(BookingStatus.CONFIRMED);
        return mapToResponse(bookingRepository.save(booking));
    }

    @Override
    public BookingResponse startRental(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + id));

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new InvalidBookingException("Cannot start rental for a booking that is " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.ACTIVE);
        
        Car car = booking.getCar();
        car.setStatus(CarStatus.RENTED);
        carRepository.save(car);

        return mapToResponse(bookingRepository.save(booking));
    }

    @Override
    public BookingResponse completeRental(Long id, ReturnCarRequest returnRequest) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + id));

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new InvalidBookingException("Booking is already completed.");
        }

        LocalDate returnDate = (returnRequest != null && returnRequest.getReturnDate() != null)
                ? returnRequest.getReturnDate()
                : LocalDate.now();

        booking.setActualReturnDate(returnDate);
        booking.setStatus(BookingStatus.COMPLETED);

        // Check for late return penalty
        if (returnDate.isAfter(booking.getEndDate())) {
            long lateDays = ChronoUnit.DAYS.between(booking.getEndDate(), returnDate);
            BigDecimal lateDailyRate = booking.getDailyRate().multiply(LATE_RETURN_DAILY_SURCHARGE);
            BigDecimal lateFee = lateDailyRate.multiply(BigDecimal.valueOf(lateDays)).setScale(2, RoundingMode.HALF_UP);
            booking.setLateFee(lateFee);
            booking.setTotalAmount(booking.getTotalAmount().add(lateFee));
        }

        // Release the car back to available (or maintenance if damaged)
        Car car = booking.getCar();
        if (returnRequest != null && returnRequest.isHasDamage()) {
            car.setStatus(CarStatus.MAINTENANCE);
        } else {
            car.setStatus(CarStatus.AVAILABLE);
        }

        if (returnRequest != null && returnRequest.getCurrentMileage() != null && returnRequest.getCurrentMileage() > car.getMileage()) {
            car.setMileage(returnRequest.getCurrentMileage());
        }
        carRepository.save(car);

        return mapToResponse(bookingRepository.save(booking));
    }

    @Override
    public BookingResponse cancelBooking(Long id, String reason) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + id));

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new InvalidBookingException("Cannot cancel an already completed booking.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        if (reason != null && !reason.trim().isEmpty()) {
            String updatedNotes = (booking.getNotes() != null ? booking.getNotes() + "\n" : "") + "Cancellation Reason: " + reason;
            booking.setNotes(updatedNotes);
        }

        // Release car if it was marked as rented
        Car car = booking.getCar();
        if (car.getStatus() == CarStatus.RENTED) {
            car.setStatus(CarStatus.AVAILABLE);
            carRepository.save(car);
        }

        return mapToResponse(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCarAvailable(Long carId, LocalDate startDate, LocalDate endDate, Long excludeBookingId) {
        List<Booking> conflicts;
        if (excludeBookingId != null) {
            conflicts = bookingRepository.findConflictingBookingsExcluding(carId, excludeBookingId, startDate, endDate);
        } else {
            conflicts = bookingRepository.findConflictingBookings(carId, startDate, endDate);
        }
        return conflicts.isEmpty();
    }

    @Override
    public BookingResponse mapToResponse(Booking booking) {
        BookingResponse res = new BookingResponse();
        res.setId(booking.getId());
        res.setBookingReference(booking.getBookingReference());

        if (booking.getCar() != null) {
            Car car = booking.getCar();
            res.setCarId(car.getId());
            res.setCarMake(car.getMake());
            res.setCarModel(car.getModel());
            res.setCarYear(car.getYear());
            res.setCarCategory(car.getCategory().name());
            res.setCarRegistrationNumber(car.getRegistrationNumber());
            res.setCarImageUrl(car.getImageUrl());
        }

        if (booking.getCustomer() != null) {
            Customer customer = booking.getCustomer();
            res.setCustomerId(customer.getId());
            res.setCustomerName(customer.getFullName());
            res.setCustomerEmail(customer.getEmail());
            res.setCustomerPhone(customer.getPhone());
            res.setCustomerDriverLicense(customer.getDriverLicenseNumber());
        }

        res.setStartDate(booking.getStartDate());
        res.setEndDate(booking.getEndDate());
        res.setActualReturnDate(booking.getActualReturnDate());
        res.setTotalDays(booking.getTotalDays());
        res.setDailyRate(booking.getDailyRate());
        res.setInsuranceFee(booking.getInsuranceFee());
        res.setDiscountAmount(booking.getDiscountAmount());
        res.setLateFee(booking.getLateFee());
        res.setTotalAmount(booking.getTotalAmount());
        res.setStatus(booking.getStatus());
        res.setPickupLocation(booking.getPickupLocation());
        res.setDropoffLocation(booking.getDropoffLocation());
        res.setNotes(booking.getNotes());
        res.setCreatedAt(booking.getCreatedAt());

        // Find associated payment
        List<Payment> payments = paymentRepository.findByBookingId(booking.getId());
        if (!payments.isEmpty()) {
            Payment p = payments.get(0);
            res.setPaymentMethod(p.getPaymentMethod());
            res.setPaymentStatus(p.getPaymentStatus());
            res.setTransactionId(p.getTransactionId());
        }

        return res;
    }

    private void validateBookingDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new InvalidBookingException("Both start date and end date are required.");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new InvalidBookingException("Start date cannot be in the past.");
        }
        if (endDate.isBefore(startDate)) {
            throw new InvalidBookingException("End date cannot be earlier than start date.");
        }
    }

    private Customer resolveCustomer(BookingRequest request) {
        if (request.getCustomerId() != null) {
            return customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + request.getCustomerId()));
        }

        if (request.getCustomerEmail() == null || request.getCustomerEmail().trim().isEmpty()) {
            throw new InvalidBookingException("Customer ID or customer details (name, email, phone, license) are required.");
        }

        // Check if customer exists by email
        return customerRepository.findByEmail(request.getCustomerEmail())
                .orElseGet(() -> {
                    if (request.getCustomerDriverLicense() == null || request.getCustomerDriverLicense().trim().isEmpty()) {
                        throw new InvalidBookingException("Driver's license number is required for new customer registration.");
                    }
                    Customer newCustomer = new Customer(
                            request.getCustomerFirstName() != null ? request.getCustomerFirstName() : "Valued",
                            request.getCustomerLastName() != null ? request.getCustomerLastName() : "Customer",
                            request.getCustomerEmail(),
                            request.getCustomerPhone() != null ? request.getCustomerPhone() : "N/A",
                            request.getCustomerDriverLicense(),
                            request.getCustomerAddress() != null ? request.getCustomerAddress() : "Main City"
                    );
                    return customerRepository.save(newCustomer);
                });
    }

    private BigDecimal calculateDiscount(String promoCode, BigDecimal baseTotal, int days) {
        if (promoCode == null || promoCode.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        String code = promoCode.trim().toUpperCase();
        switch (code) {
            case "SAVE10":
                return baseTotal.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
            case "DRIVE20":
                return new BigDecimal("20.00").min(baseTotal);
            case "WEEKEND":
                if (days >= 3) {
                    return baseTotal.multiply(new BigDecimal("0.15")).setScale(2, RoundingMode.HALF_UP);
                }
                return BigDecimal.ZERO;
            case "SPECIAL":
                return new BigDecimal("50.00").min(baseTotal);
            default:
                return BigDecimal.ZERO;
        }
    }
}
