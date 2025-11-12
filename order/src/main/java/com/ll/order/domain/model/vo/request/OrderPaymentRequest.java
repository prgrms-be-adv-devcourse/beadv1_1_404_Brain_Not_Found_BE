package com.ll.order.domain.model.vo.request;

import com.ll.payment.model.enums.PaidType;

public record OrderPaymentRequest(
        Long orderId,
        Long buyerId,
        int paidAmount,
        PaidType paidType,
        String paymentKey // 토스 승인용
) {
}
