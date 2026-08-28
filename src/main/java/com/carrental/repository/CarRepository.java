package com.carrental.repository;

import com.carrental.entity.Car;
import com.carrental.enums.CarCategory;
import com.carrental.enums.CarStatus;
import com.carrental.enums.FuelType;
import com.carrental.enums.Transmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    List<Car> findByStatus(CarStatus status);

    List<Car> findByCategory(CarCategory category);

    Optional<Car> findByRegistrationNumber(String registrationNumber);

    boolean existsByRegistrationNumber(String registrationNumber);

    @Query("SELECT c FROM Car c WHERE " +
           "(:category IS NULL OR c.category = :category) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:fuelType IS NULL OR c.fuelType = :fuelType) AND " +
           "(:transmission IS NULL OR c.transmission = :transmission) AND " +
           "(:maxRate IS NULL OR c.dailyRate <= :maxRate) AND " +
           "(:search IS NULL OR LOWER(c.make) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.model) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Car> filterCars(@Param("category") CarCategory category,
                         @Param("status") CarStatus status,
                         @Param("fuelType") FuelType fuelType,
                         @Param("transmission") Transmission transmission,
                         @Param("maxRate") BigDecimal maxRate,
                         @Param("search") String search);

    long countByStatus(CarStatus status);
}
