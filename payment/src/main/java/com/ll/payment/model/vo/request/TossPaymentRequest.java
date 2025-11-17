package com.ll.payment.model.vo.request;

public record TossPaymentRequest(
        String paymentKey,
        String orderId,
        int amount
) {
}
