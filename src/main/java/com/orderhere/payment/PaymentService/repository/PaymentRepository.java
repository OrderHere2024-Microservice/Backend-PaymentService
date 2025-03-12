package com.orderhere.payment.PaymentService.repository;

import com.orderhere.payment.PaymentService.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Payment getByPaymentId(Integer paymentId);
}
