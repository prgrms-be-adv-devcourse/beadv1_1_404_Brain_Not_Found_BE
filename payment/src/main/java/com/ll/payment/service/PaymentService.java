package com.ll.payment.service;

import com.ll.payment.model.entity.Payment;
import com.ll.payment.model.vo.PaymentRequest;
import com.ll.payment.model.vo.TossPaymentRequest;

public interface PaymentService {
    String confirmPayment(TossPaymentRequest request);

    void depositPayment(PaymentRequest payment);

    Payment tossPayment(PaymentRequest payment);
}
