package com.carrental.service;

import com.carrental.dto.PaymentRequest;
import com.carrental.entity.Payment;

import java.util.List;

public interface PaymentService {

    Payment processPayment(PaymentRequest paymentRequest);

    List<Payment> getPaymentsByBookingId(Long bookingId);

    Payment getPaymentByTransactionId(String transactionId);
}
