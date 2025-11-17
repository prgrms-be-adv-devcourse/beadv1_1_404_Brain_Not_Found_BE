package com.ll.payment.model.vo.request;

public record PaymentCreateRequest(
        Long orderId,
        String orderName,
        String customerName,
        Integer amount
) {
}

