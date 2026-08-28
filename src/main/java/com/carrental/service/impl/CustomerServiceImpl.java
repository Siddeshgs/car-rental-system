package com.carrental.service.impl;

import com.carrental.dto.CustomerDto;
import com.carrental.entity.Customer;
import com.carrental.exception.ResourceNotFoundException;
import com.carrental.repository.CustomerRepository;
import com.carrental.service.CustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));
    }

    @Override
    public Customer createCustomer(CustomerDto customerDto) {
        if (customerRepository.existsByEmail(customerDto.getEmail())) {
            throw new IllegalArgumentException("Customer with email '" + customerDto.getEmail() + "' already exists.");
        }
        if (customerRepository.existsByDriverLicenseNumber(customerDto.getDriverLicenseNumber())) {
            throw new IllegalArgumentException("Driver's license '" + customerDto.getDriverLicenseNumber() + "' is already registered.");
        }

        Customer customer = new Customer(
                customerDto.getFirstName(),
                customerDto.getLastName(),
                customerDto.getEmail(),
                customerDto.getPhone(),
                customerDto.getDriverLicenseNumber(),
                customerDto.getAddress()
        );

        return customerRepository.save(customer);
    }

    @Override
    public Customer updateCustomer(Long id, CustomerDto customerDto) {
        Customer customer = getCustomerById(id);

        if (!customer.getEmail().equalsIgnoreCase(customerDto.getEmail())
                && customerRepository.existsByEmail(customerDto.getEmail())) {
            throw new IllegalArgumentException("Email '" + customerDto.getEmail() + "' is already in use by another customer.");
        }

        if (!customer.getDriverLicenseNumber().equalsIgnoreCase(customerDto.getDriverLicenseNumber())
                && customerRepository.existsByDriverLicenseNumber(customerDto.getDriverLicenseNumber())) {
            throw new IllegalArgumentException("Driver's license '" + customerDto.getDriverLicenseNumber() + "' is already in use.");
        }

        customer.setFirstName(customerDto.getFirstName());
        customer.setLastName(customerDto.getLastName());
        customer.setEmail(customerDto.getEmail());
        customer.setPhone(customerDto.getPhone());
        customer.setDriverLicenseNumber(customerDto.getDriverLicenseNumber());
        customer.setAddress(customerDto.getAddress());

        return customerRepository.save(customer);
    }

    @Override
    public void deleteCustomer(Long id) {
        Customer customer = getCustomerById(id);
        customerRepository.delete(customer);
    }

    @Override
    public CustomerDto mapToDto(Customer customer) {
        return new CustomerDto(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getDriverLicenseNumber(),
                customer.getAddress(),
                customer.getCreatedAt()
        );
    }
}
