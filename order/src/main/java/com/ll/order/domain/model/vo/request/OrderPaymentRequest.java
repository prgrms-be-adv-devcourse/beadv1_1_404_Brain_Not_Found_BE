package com.ll.order.domain.model.vo.request;

import com.ll.payment.model.enums.PaidType;

public record OrderPaymentRequest(
        Long orderId,
        String orderCode,
        Long buyerId,
        String buyerCode,
        int paidAmount,
        PaidType paidType,
        String paymentKey // 토스 승인용
) {
}
