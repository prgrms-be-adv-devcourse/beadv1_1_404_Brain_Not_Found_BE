package com.ll.payment.service;

import com.ll.payment.model.entity.Payment;
import com.ll.payment.model.vo.PaymentProcessResult;
import com.ll.payment.model.vo.request.PaymentRefundRequest;
import com.ll.payment.model.vo.request.PaymentRequest;
import com.ll.payment.model.vo.request.TossPaymentRequest;

public interface PaymentService {
    String confirmPayment(TossPaymentRequest request);

    PaymentProcessResult depositPayment(PaymentRequest payment);

    Payment tossPayment(PaymentRequest payment);

    Payment refundPayment(PaymentRefundRequest request);

    String createPayment(Long orderId, String orderName, String customerName, Integer amount);
}
