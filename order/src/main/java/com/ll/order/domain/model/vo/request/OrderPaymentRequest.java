package com.ll.order.domain.model.vo.request;

import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.enums.payment.PaidType;

public record OrderPaymentRequest(
        Long orderId,
        String orderCode,
        Long buyerId,
        String buyerCode,
        int paidAmount,
        PaidType paidType,
        String paymentKey // 토스 승인용
) {
    public static OrderPaymentRequest from(Order order, String buyerCode, PaidType paidType, String paymentKey) {
        return new OrderPaymentRequest(
                order.getId(),
                order.getCode(),
                order.getBuyerId(),
                buyerCode,
                order.getTotalPrice(),
                paidType,
                paymentKey
        );
    }
}
