package com.carrental.controller;

import com.carrental.dto.ApiResponse;
import com.carrental.dto.CustomerDto;
import com.carrental.entity.Customer;
import com.carrental.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
@Tag(name = "Customer Management", description = "Endpoints for customer profiles and registration")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @Operation(summary = "Get all registered customers")
    public ResponseEntity<ApiResponse<List<CustomerDto>>> getAllCustomers() {
        List<CustomerDto> list = customerService.getAllCustomers()
                .stream()
                .map(customerService::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok("Customers retrieved", list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<ApiResponse<CustomerDto>> getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.ok("Customer details retrieved", customerService.mapToDto(customer)));
    }

    @PostMapping
    @Operation(summary = "Register a new customer")
    public ResponseEntity<ApiResponse<CustomerDto>> createCustomer(@Valid @RequestBody CustomerDto customerDto) {
        Customer created = customerService.createCustomer(customerDto);
        return new ResponseEntity<>(ApiResponse.ok("Customer registered successfully", customerService.mapToDto(created)), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer profile")
    public ResponseEntity<ApiResponse<CustomerDto>> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerDto customerDto) {
        Customer updated = customerService.updateCustomer(id, customerDto);
        return ResponseEntity.ok(ApiResponse.ok("Customer updated successfully", customerService.mapToDto(updated)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer profile")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.ok("Customer deleted successfully", null));
    }
}
