package com.carrental.service.impl;

import com.carrental.dto.PaymentRequest;
import com.carrental.entity.Booking;
import com.carrental.entity.Payment;
import com.carrental.enums.PaymentStatus;
import com.carrental.exception.ResourceNotFoundException;
import com.carrental.repository.BookingRepository;
import com.carrental.repository.PaymentRepository;
import com.carrental.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository, BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Payment processPayment(PaymentRequest paymentRequest) {
        Booking booking = bookingRepository.findById(paymentRequest.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + paymentRequest.getBookingId()));

        String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

        Payment payment = new Payment(
                booking,
                paymentRequest.getAmount(),
                paymentRequest.getPaymentMethod(),
                PaymentStatus.PAID,
                txnId,
                LocalDateTime.now()
        );

        return paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with Transaction ID: " + transactionId));
    }
}
