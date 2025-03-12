package com.orderhere.payment.PaymentService.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResultDto {
    private Integer paymentId;
    private String result;
}
