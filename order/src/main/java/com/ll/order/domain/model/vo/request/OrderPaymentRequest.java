package com.ll.order.domain.model.vo.request;


public record OrderPaymentRequest(
        String orderCode,
        String userCode,
        Long paidAmount
//        PaidType paidType
) {
}
