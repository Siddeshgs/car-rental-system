package com.carrental.service;

import com.carrental.dto.CustomerDto;
import com.carrental.entity.Customer;

import java.util.List;

public interface CustomerService {

    List<Customer> getAllCustomers();

    Customer getCustomerById(Long id);

    Customer getCustomerByEmail(String email);

    Customer createCustomer(CustomerDto customerDto);

    Customer updateCustomer(Long id, CustomerDto customerDto);

    void deleteCustomer(Long id);

    CustomerDto mapToDto(Customer customer);
}
