package com.carrental.controller;

import com.carrental.dto.ApiResponse;
import com.carrental.dto.BookingRequest;
import com.carrental.dto.BookingResponse;
import com.carrental.dto.ReturnCarRequest;
import com.carrental.enums.BookingStatus;
import com.carrental.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
@Tag(name = "Booking Workflow", description = "Endpoints for booking lifecycle, reservations, returns, cancellations")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    @Operation(summary = "Get all bookings or filter by status or customer")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) Long customerId) {

        List<BookingResponse> bookings;
        if (status != null) {
            bookings = bookingService.getBookingsByStatus(status);
        } else if (customerId != null) {
            bookings = bookingService.getBookingsByCustomer(customerId);
        } else {
            bookings = bookingService.getAllBookings();
        }

        return ResponseEntity.ok(ApiResponse.ok("Bookings retrieved", bookings));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(@PathVariable Long id) {
        BookingResponse booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(ApiResponse.ok("Booking details retrieved", booking));
    }

    @GetMapping("/reference/{reference}")
    @Operation(summary = "Get booking by unique booking reference code")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingByReference(@PathVariable String reference) {
        BookingResponse booking = bookingService.getBookingByReference(reference);
        return ResponseEntity.ok(ApiResponse.ok("Booking retrieved", booking));
    }

    @GetMapping("/check-availability")
    @Operation(summary = "Check if a car is available for a date range")
    public ResponseEntity<ApiResponse<Boolean>> checkAvailability(
            @RequestParam Long carId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        boolean available = bookingService.isCarAvailable(carId, startDate, endDate, null);
        return ResponseEntity.ok(ApiResponse.ok(available ? "Car is available" : "Car is not available", available));
    }

    @PostMapping
    @Operation(summary = "Create a new car booking/reservation")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(@Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return new ResponseEntity<>(ApiResponse.ok("Booking confirmed successfully", response), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/confirm")
    @Operation(summary = "Confirm a pending booking")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(@PathVariable Long id) {
        BookingResponse response = bookingService.confirmBooking(id);
        return ResponseEntity.ok(ApiResponse.ok("Booking confirmed", response));
    }

    @PatchMapping("/{id}/start")
    @Operation(summary = "Hand over car to customer (marks booking as ACTIVE and car as RENTED)")
    public ResponseEntity<ApiResponse<BookingResponse>> startRental(@PathVariable Long id) {
        BookingResponse response = bookingService.startRental(id);
        return ResponseEntity.ok(ApiResponse.ok("Rental started successfully. Car handed over to customer.", response));
    }

    @PostMapping("/{id}/return")
    @Operation(summary = "Process car return (calculates late fees if any, marks booking as COMPLETED and car as AVAILABLE)")
    public ResponseEntity<ApiResponse<BookingResponse>> completeRental(
            @PathVariable Long id,
            @RequestBody(required = false) ReturnCarRequest returnRequest) {
        BookingResponse response = bookingService.completeRental(id, returnRequest);
        return ResponseEntity.ok(ApiResponse.ok("Car returned successfully. Booking completed.", response));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking reservation")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        BookingResponse response = bookingService.cancelBooking(id, reason);
        return ResponseEntity.ok(ApiResponse.ok("Booking cancelled successfully", response));
    }
}
