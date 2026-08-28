package com.carrental.repository;

import com.carrental.entity.Booking;
import com.carrental.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Booking> findByCarIdOrderByCreatedAtDesc(Long carId);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findAllByOrderByCreatedAtDesc();

    @Query("SELECT b FROM Booking b WHERE b.car.id = :carId " +
           "AND b.status NOT IN ('CANCELLED', 'COMPLETED') " +
           "AND (:startDate <= b.endDate AND :endDate >= b.startDate)")
    List<Booking> findConflictingBookings(@Param("carId") Long carId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    @Query("SELECT b FROM Booking b WHERE b.car.id = :carId " +
           "AND b.id <> :excludeBookingId " +
           "AND b.status NOT IN ('CANCELLED', 'COMPLETED') " +
           "AND (:startDate <= b.endDate AND :endDate >= b.startDate)")
    List<Booking> findConflictingBookingsExcluding(@Param("carId") Long carId,
                                                  @Param("excludeBookingId") Long excludeBookingId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    long countByStatus(BookingStatus status);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.status IN ('CONFIRMED', 'ACTIVE', 'COMPLETED')")
    BigDecimal calculateTotalRevenue();
}
