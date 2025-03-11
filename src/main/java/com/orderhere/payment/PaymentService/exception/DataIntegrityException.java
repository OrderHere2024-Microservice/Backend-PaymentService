package com.orderhere.payment.PaymentService.exception;

public class DataIntegrityException extends RuntimeException {
  public DataIntegrityException(String message) {
    super(message);
  }
}
