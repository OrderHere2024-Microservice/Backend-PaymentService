package com.orderhere.payment.PaymentService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class PaymentPostDto {
    private Integer orderId;
    private BigDecimal amount;
    private String currency;
}
