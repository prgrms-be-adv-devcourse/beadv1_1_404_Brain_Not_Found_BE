package com.ll.payment.payment.service.toss;

import com.ll.payment.payment.model.entity.Payment;
import com.ll.payment.payment.model.enums.PaymentStatus;
import com.ll.payment.payment.model.vo.request.PaymentRequest;

public interface TossPaymentService {

    Payment tossPayment(PaymentRequest request, PaymentStatus finalStatus);

    Payment chargeDepositWithToss(PaymentRequest payment, int chargeAmount);
}

