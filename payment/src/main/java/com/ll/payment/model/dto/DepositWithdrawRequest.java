package com.ll.payment.model.dto;

public record DepositWithdrawRequest(
        Integer amount,
        String referenceCode
) {
}

