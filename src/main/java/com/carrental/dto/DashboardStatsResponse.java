package com.carrental.dto;

import java.math.BigDecimal;

public class DashboardStatsResponse {

    private long totalCars;
    private long availableCars;
    private long rentedCars;
    private long maintenanceCars;
    private long totalCustomers;
    private long totalBookings;
    private long activeBookings;
    private long completedBookings;
    private BigDecimal totalRevenue;
    private double fleetUtilizationRate;

    public DashboardStatsResponse() {
    }

    public long getTotalCars() {
        return totalCars;
    }

    public void setTotalCars(long totalCars) {
        this.totalCars = totalCars;
    }

    public long getAvailableCars() {
        return availableCars;
    }

    public void setAvailableCars(long availableCars) {
        this.availableCars = availableCars;
    }

    public long getRentedCars() {
        return rentedCars;
    }

    public void setRentedCars(long rentedCars) {
        this.rentedCars = rentedCars;
    }

    public long getMaintenanceCars() {
        return maintenanceCars;
    }

    public void setMaintenanceCars(long maintenanceCars) {
        this.maintenanceCars = maintenanceCars;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(long totalBookings) {
        this.totalBookings = totalBookings;
    }

    public long getActiveBookings() {
        return activeBookings;
    }

    public void setActiveBookings(long activeBookings) {
        this.activeBookings = activeBookings;
    }

    public long getCompletedBookings() {
        return completedBookings;
    }

    public void setCompletedBookings(long completedBookings) {
        this.completedBookings = completedBookings;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getFleetUtilizationRate() {
        return fleetUtilizationRate;
    }

    public void setFleetUtilizationRate(double fleetUtilizationRate) {
        this.fleetUtilizationRate = fleetUtilizationRate;
    }
}
