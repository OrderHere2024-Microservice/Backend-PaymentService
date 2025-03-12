package com.orderhere.payment.PaymentService.service;


import com.orderhere.payment.PaymentService.dto.PaymentCreateDto;
import com.orderhere.payment.PaymentService.dto.PaymentPostDto;
import com.orderhere.payment.PaymentService.dto.PaymentResultDto;
import com.orderhere.payment.PaymentService.enums.PaymentStatus;
import com.orderhere.payment.PaymentService.model.Payment;
import com.orderhere.payment.PaymentService.repository.PaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentCreateDto createPayment(PaymentPostDto paymentPostDto) throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(paymentPostDto.getAmount().multiply(new BigDecimal("100")).longValue())
                .setCurrency(paymentPostDto.getCurrency())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods
                                .builder()
                                .setEnabled(true)
                                .build()
                )
                .build();

        log.info("trying to create paymentIntent for order: {}", paymentPostDto.getOrderId());
        PaymentIntent paymentIntent = PaymentIntent.create(params);
        System.out.println(paymentIntent.getClientSecret());
        Payment payment = new Payment();
        payment.setPaymentMethod(paymentIntent.getPaymentMethod());
        payment.setPaymentStatus(PaymentStatus.unpaid);
        payment.setStripePaymentId(paymentIntent.getId());
        payment.setOrderId(paymentPostDto.getOrderId());
        payment.setCurrency(paymentPostDto.getCurrency());
        payment.setAmount(paymentPostDto.getAmount());
        return new PaymentCreateDto(
                paymentRepository.save(payment).getPaymentId(),
                paymentIntent.getClientSecret()
        );
    }

    @Transactional
    public void getPaymentResult(PaymentResultDto paymentResultDto) throws StripeException {
        Payment payment = paymentRepository.getByPaymentId(paymentResultDto.getPaymentId());
        if (paymentResultDto.getResult().equals("success")) {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(payment.getStripePaymentId());
            payment.setPaymentMethod(paymentIntent.getPaymentMethod());
            payment.setPaymentStatus(PaymentStatus.paid);
            paymentRepository.save(payment);
            Integer orderId = payment.getOrderId();
//            order.setOrderStatus(OrderStatus.preparing);
//            orderRepository.save(order);
            /*
            * Let's use kafka here to send a message to the order service to update the order status
            * */
        }
        else {
            payment.setPaymentStatus(PaymentStatus.failed);
            paymentRepository.save(payment);
        }
    }
}
