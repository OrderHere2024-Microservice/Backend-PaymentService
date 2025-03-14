package com.orderhere.payment.PaymentService.service;


import com.orderhere.payment.PaymentService.dto.PaymentCreateDto;
import com.orderhere.payment.PaymentService.dto.PaymentPostDto;
import com.orderhere.payment.PaymentService.dto.PaymentResultDto;
import com.orderhere.payment.PaymentService.enums.PaymentStatus;
import com.orderhere.payment.PaymentService.eventDto.PaymentSuccessEvent;
import com.orderhere.payment.PaymentService.model.Payment;
import com.orderhere.payment.PaymentService.repository.PaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, PaymentSuccessEvent> kafkaTemplate;

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
    public void getPaymentResult(PaymentResultDto paymentResultDto) throws Exception {
        Payment payment = paymentRepository.getByPaymentId(paymentResultDto.getPaymentId());
        if (paymentResultDto.getResult().equals("success")) {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(payment.getStripePaymentId());
            payment.setPaymentMethod(paymentIntent.getPaymentMethod());
            payment.setPaymentStatus(PaymentStatus.paid);
            paymentRepository.save(payment);

            PaymentSuccessEvent paymentSuccessEvent = new PaymentSuccessEvent(payment.getPaymentId().toString(), payment.getOrderId().toString());
            try {
                kafkaTemplate.send("payment-success-topic", paymentSuccessEvent).get();
            } catch (Exception e) {
                log.error("Error occurred while sending payment success event", e);
                throw new Exception("Error occurred while sending payment success event");
            }
        } else {
            payment.setPaymentStatus(PaymentStatus.failed);
            paymentRepository.save(payment);
        }
    }
}
