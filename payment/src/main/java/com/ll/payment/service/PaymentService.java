package com.ll.payment.service;

import com.ll.payment.model.entity.Payment;
import com.ll.payment.model.enums.PaymentStatus;
import com.ll.payment.model.vo.PaymentProcessResult;
import com.ll.payment.model.vo.request.PaymentRefundRequest;
import com.ll.payment.model.vo.request.PaymentRequest;

public interface PaymentService {
    PaymentProcessResult depositPayment(PaymentRequest payment);

    Payment tossPayment(PaymentRequest request, PaymentStatus finalStatus);

    Payment refundPayment(PaymentRefundRequest request);
}
