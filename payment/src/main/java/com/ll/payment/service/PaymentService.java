package com.ll.payment.service;

import com.ll.payment.model.vo.TossPaymentRequest;

public interface PaymentService {
    String confirmPayment(TossPaymentRequest request);
}
