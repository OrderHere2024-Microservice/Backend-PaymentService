package com.orderhere.payment.PaymentService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PaymentCreateDto {
    private Integer paymentId;
    private String clientSecret;
}
