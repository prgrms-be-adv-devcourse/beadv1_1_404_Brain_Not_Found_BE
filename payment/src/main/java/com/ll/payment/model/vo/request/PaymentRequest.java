package com.ll.payment.model.vo.request;

import com.ll.payment.model.enums.PaidType;

public record PaymentRequest(
        Long orderId,
        Long buyerId,
        String buyerCode,
        int paidAmount,
        PaidType paidType,
        String paymentKey // 토스 승인용
) {
}
