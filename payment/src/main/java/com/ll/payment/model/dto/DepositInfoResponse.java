package com.ll.payment.model.dto;

public record DepositInfoResponse(
        String userCode,
        Long balance
) {
}

