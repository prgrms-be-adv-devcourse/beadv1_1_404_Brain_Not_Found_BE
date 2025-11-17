package com.ll.payment.model.vo.request;

import com.ll.payment.model.enums.PaidType;

public record PaymentRefundRequest(
        Long paymentId,
        String paymentCode,
        Long orderId,
        String orderCode,
        Integer refundAmount,
        String reason,
        PaidType paidType,
        String paymentKey,
        String buyerCode
) {
}

