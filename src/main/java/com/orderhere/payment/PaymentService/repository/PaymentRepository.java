package com.orderhere.payment.PaymentService.repository;

import com.orderhere.payment.PaymentService.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Payment getByPaymentId(Integer paymentId);

    // Current one order has 2 payments
    List<Payment> getByOrderOrderId(Integer orderId);
}
