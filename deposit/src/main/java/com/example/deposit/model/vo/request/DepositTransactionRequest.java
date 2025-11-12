package com.example.deposit.model.vo.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DepositTransactionRequest(
        @NotNull(message = "amount 는 필수입력값입니다.")
        @Min(value = 1, message = "금액은 0보다 커야 합니다.")
        Long amount,
        String referenceCode
) {
    public static DepositTransactionRequest of(Long amount, String referenceCode) {
        return new DepositTransactionRequest(amount, referenceCode);
    }
}
