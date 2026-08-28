package com.carrental.service;

import com.carrental.dto.CustomerDto;
import com.carrental.entity.Customer;
import com.carrental.exception.ResourceNotFoundException;
import com.carrental.repository.CustomerRepository;
import com.carrental.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer sampleCustomer;
    private CustomerDto sampleDto;

    @BeforeEach
    void setUp() {
        sampleCustomer = new Customer("Alice", "Smith", "alice@example.com", "+1 555-4321", "DL-998877", "456 Oak Lane");
        sampleCustomer.setId(1L);

        sampleDto = new CustomerDto(null, "Alice", "Smith", "alice@example.com", "+1 555-4321", "DL-998877", "456 Oak Lane", null);
    }

    @Test
    @DisplayName("Should create customer successfully")
    void testCreateCustomer_Success() {
        when(customerRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(customerRepository.existsByDriverLicenseNumber("DL-998877")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        Customer created = customerService.createCustomer(sampleDto);

        assertNotNull(created);
        assertEquals("Alice", created.getFirstName());
        assertEquals("alice@example.com", created.getEmail());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when email already exists")
    void testCreateCustomer_DuplicateEmail_ThrowsException() {
        when(customerRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(sampleDto));
        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fetch customer by ID")
    void testGetCustomerById_Success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));

        Customer found = customerService.getCustomerById(1L);

        assertNotNull(found);
        assertEquals("Alice", found.getFirstName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when customer not found")
    void testGetCustomerById_NotFound_ThrowsException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerById(99L));
    }
}
