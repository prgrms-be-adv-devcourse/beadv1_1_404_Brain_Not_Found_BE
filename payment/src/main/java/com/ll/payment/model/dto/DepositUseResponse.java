package com.ll.payment.model.dto;

public record DepositUseResponse(
        long historyId,
        long remainingBalance
) {
}

