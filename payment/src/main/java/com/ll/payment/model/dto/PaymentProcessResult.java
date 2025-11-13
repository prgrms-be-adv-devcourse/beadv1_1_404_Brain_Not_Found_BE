package com.ll.payment.model.dto;

import com.ll.payment.model.entity.Payment;

public record PaymentProcessResult(
        Payment depositPayment,
        Payment tossPayment
) {
}

