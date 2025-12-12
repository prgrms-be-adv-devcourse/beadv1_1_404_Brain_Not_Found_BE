package com.ll.payment.payment.model.vo;

public record PaymentRefundNotificationPayload(
        String orderCode,
        String status
) {
}

