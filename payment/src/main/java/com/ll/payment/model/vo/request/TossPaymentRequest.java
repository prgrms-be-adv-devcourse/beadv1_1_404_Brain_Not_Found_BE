package com.ll.payment.model.vo.request;

public record TossPaymentRequest(
        String paymentKey,
        String orderId,
        int amount
) {
    public static TossPaymentRequest from(String paymentKey, String orderCode, int paidAmount) {
        return new TossPaymentRequest(
                paymentKey,
                orderCode,
                paidAmount
        );
    }
}
