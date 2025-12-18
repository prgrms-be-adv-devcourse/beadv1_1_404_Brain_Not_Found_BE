package com.ll.payment.payment.service.deposit;

import com.ll.payment.payment.model.entity.Payment;
import com.ll.payment.payment.model.vo.PaymentProcessResult;
import com.ll.payment.payment.model.vo.request.PaymentRequest;

public interface DepositPaymentService {

    PaymentProcessResult depositPayment(PaymentRequest payment);

    Payment completeDepositPayment(PaymentRequest payment, int amount);

    // 토스 결제 완료 후 예치금 충전
    void chargeDepositAfterToss(String buyerCode, int amount, String referenceCode);
}

