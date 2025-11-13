package com.ll.payment.model.dto;

public record DepositWithdrawRequest(
        Long amount,
        String referenceCode
) {
}

