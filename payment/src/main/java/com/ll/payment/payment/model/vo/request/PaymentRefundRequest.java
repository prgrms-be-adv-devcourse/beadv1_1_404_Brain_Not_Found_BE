package com.ll.payment.payment.model.vo.request;

import com.ll.payment.payment.model.enums.PaidType;

public record PaymentRefundRequest(
        Long paymentId,
        String paymentCode,
        Long orderId,
        String orderCode,
        Integer refundAmount,
        String reason,
        PaidType paidType,
        String buyerCode
) {
}

