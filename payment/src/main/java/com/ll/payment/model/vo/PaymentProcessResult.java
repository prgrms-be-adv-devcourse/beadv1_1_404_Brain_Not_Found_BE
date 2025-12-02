package com.ll.payment.model.vo;

import com.ll.payment.model.entity.Payment;

public record PaymentProcessResult(
        Payment depositPayment,
        Payment tossPayment
) {
}

