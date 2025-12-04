package com.ll.payment.payment.service.refund;

import com.ll.payment.payment.model.entity.Payment;
import com.ll.payment.payment.model.vo.request.PaymentRefundRequest;

public interface PaymentRefundService {

    Payment refundPayment(PaymentRefundRequest request);

    void processTossRefundForCharge(Payment payment, int refundAmount);
}

