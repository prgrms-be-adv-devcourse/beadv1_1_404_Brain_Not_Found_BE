package com.ll.payment.model.vo;

public record TossPaymentRequest(
        String paymentKey,
        String orderId,
        int amount
) {
}
