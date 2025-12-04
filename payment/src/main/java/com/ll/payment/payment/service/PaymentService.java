package com.ll.payment.payment.service;

import com.ll.payment.payment.model.entity.Payment;
import com.ll.payment.payment.model.enums.PaymentStatus;
import com.ll.payment.payment.model.vo.PaymentProcessResult;
import com.ll.payment.payment.model.vo.request.PaymentRefundRequest;
import com.ll.payment.payment.model.vo.request.PaymentRequest;

public interface PaymentService {
    PaymentProcessResult depositPayment(PaymentRequest payment);

    Payment tossPayment(PaymentRequest request, PaymentStatus finalStatus);

    Payment refundPayment(PaymentRefundRequest request);

}
