package com.ll.order.domain.model.vo.request;

import com.example.bnfOrderDomain.payment.PaidType;

public record OrderPaymentRequest(
        String orderCode,
        String userCode,
        Long paidAmount,
        PaidType paidType
) {
}
