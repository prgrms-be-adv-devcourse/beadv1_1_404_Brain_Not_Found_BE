package com.ll.payment.model.dto;

public record DepositBalanceResponse(
        String buyerCode,
        long balance
) {
}

