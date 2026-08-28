package com.carrental.controller;

import com.carrental.dto.ApiResponse;
import com.carrental.dto.PaymentRequest;
import com.carrental.entity.Payment;
import com.carrental.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
@Tag(name = "Payment Management", description = "Endpoints for processing and querying rental payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @Operation(summary = "Process a payment for a booking")
    public ResponseEntity<ApiResponse<Payment>> processPayment(@Valid @RequestBody PaymentRequest request) {
        Payment payment = paymentService.processPayment(request);
        return new ResponseEntity<>(ApiResponse.ok("Payment processed successfully", payment), HttpStatus.CREATED);
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get payment history for a specific booking")
    public ResponseEntity<ApiResponse<List<Payment>>> getPaymentsByBooking(@PathVariable Long bookingId) {
        List<Payment> payments = paymentService.getPaymentsByBookingId(bookingId);
        return ResponseEntity.ok(ApiResponse.ok("Payments retrieved", payments));
    }

    @GetMapping("/transaction/{txnId}")
    @Operation(summary = "Get payment record by transaction ID")
    public ResponseEntity<ApiResponse<Payment>> getPaymentByTxn(@PathVariable String txnId) {
        Payment payment = paymentService.getPaymentByTransactionId(txnId);
        return ResponseEntity.ok(ApiResponse.ok("Payment retrieved", payment));
    }
}
