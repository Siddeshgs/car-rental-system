package com.carrental.service;

import com.carrental.dto.BookingRequest;
import com.carrental.dto.BookingResponse;
import com.carrental.dto.ReturnCarRequest;
import com.carrental.entity.Booking;
import com.carrental.enums.BookingStatus;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    List<BookingResponse> getAllBookings();

    BookingResponse getBookingById(Long id);

    BookingResponse getBookingByReference(String reference);

    List<BookingResponse> getBookingsByCustomer(Long customerId);

    List<BookingResponse> getBookingsByStatus(BookingStatus status);

    BookingResponse createBooking(BookingRequest request);

    BookingResponse confirmBooking(Long id);

    BookingResponse startRental(Long id);

    BookingResponse completeRental(Long id, ReturnCarRequest returnRequest);

    BookingResponse cancelBooking(Long id, String reason);

    boolean isCarAvailable(Long carId, LocalDate startDate, LocalDate endDate, Long excludeBookingId);

    BookingResponse mapToResponse(Booking booking);
}
