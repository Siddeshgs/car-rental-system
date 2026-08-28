package com.carrental.repository;

import com.carrental.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByDriverLicenseNumber(String driverLicenseNumber);

    boolean existsByEmail(String email);

    boolean existsByDriverLicenseNumber(String driverLicenseNumber);
}
