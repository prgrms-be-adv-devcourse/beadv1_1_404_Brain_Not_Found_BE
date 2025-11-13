package com.ll.payment.model.dto;

public record DepositUseRequest(
        String buyerCode,
        long amount,
        Long orderId,
        String description
) {
}

