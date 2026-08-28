package com.carrental.service.impl;

import com.carrental.dto.DashboardStatsResponse;
import com.carrental.enums.BookingStatus;
import com.carrental.enums.CarStatus;
import com.carrental.repository.BookingRepository;
import com.carrental.repository.CarRepository;
import com.carrental.repository.CustomerRepository;
import com.carrental.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;
    private final BookingRepository bookingRepository;

    public DashboardServiceImpl(CarRepository carRepository,
                                CustomerRepository customerRepository,
                                BookingRepository bookingRepository) {
        this.carRepository = carRepository;
        this.customerRepository = customerRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public DashboardStatsResponse getDashboardStats() {
        DashboardStatsResponse stats = new DashboardStatsResponse();

        long totalCars = carRepository.count();
        long availableCars = carRepository.countByStatus(CarStatus.AVAILABLE);
        long rentedCars = carRepository.countByStatus(CarStatus.RENTED);
        long maintenanceCars = carRepository.countByStatus(CarStatus.MAINTENANCE);

        long totalCustomers = customerRepository.count();
        long totalBookings = bookingRepository.count();
        long activeBookings = bookingRepository.countByStatus(BookingStatus.ACTIVE);
        long completedBookings = bookingRepository.countByStatus(BookingStatus.COMPLETED);
        BigDecimal totalRevenue = bookingRepository.calculateTotalRevenue();

        double utilizationRate = 0.0;
        if (totalCars > 0) {
            utilizationRate = ((double) rentedCars / totalCars) * 100.0;
        }

        stats.setTotalCars(totalCars);
        stats.setAvailableCars(availableCars);
        stats.setRentedCars(rentedCars);
        stats.setMaintenanceCars(maintenanceCars);
        stats.setTotalCustomers(totalCustomers);
        stats.setTotalBookings(totalBookings);
        stats.setActiveBookings(activeBookings);
        stats.setCompletedBookings(completedBookings);
        stats.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        stats.setFleetUtilizationRate(Math.round(utilizationRate * 100.0) / 100.0);

        return stats;
    }
}
