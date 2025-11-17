package com.ll.deposit.model.vo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DepositTransactionRequest(
        @NotNull(message = "amount 는 필수입력값입니다.")
        @Positive(message = "금액는 0 보다 커야 합니다.")
        Long amount,
        @NotBlank(message = "referenceCode 는 공백이거나 null일 수 없습니다.")
        String referenceCode
) {
    public static DepositTransactionRequest of(Long amount, String referenceCode) {
        return new DepositTransactionRequest(amount, referenceCode);
    }
}
