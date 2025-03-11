package com.orderhere.payment.PaymentService.exception;

public class DataIntegrityViolationException extends RuntimeException {

  public DataIntegrityViolationException(String message) {
    super(message);
  }
}
