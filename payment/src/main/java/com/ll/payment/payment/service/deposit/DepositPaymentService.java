package com.ll.payment.payment.service.deposit;

import com.ll.payment.payment.model.entity.Payment;
import com.ll.payment.payment.model.vo.PaymentProcessResult;
import com.ll.payment.payment.model.vo.request.PaymentRequest;

public interface DepositPaymentService {

    PaymentProcessResult depositPayment(PaymentRequest payment);

    Payment completeDepositPayment(PaymentRequest payment, int amount);
}

