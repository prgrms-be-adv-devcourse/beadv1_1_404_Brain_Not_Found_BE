package com.ll.payment.payment.model.vo;

import com.ll.payment.payment.model.entity.Payment;

public record PaymentProcessResult(
        Payment depositPayment,
        Payment tossPayment
) {
}

