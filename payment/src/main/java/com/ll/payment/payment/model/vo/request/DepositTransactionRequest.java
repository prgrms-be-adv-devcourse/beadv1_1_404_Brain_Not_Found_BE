package com.ll.payment.payment.model.vo.request;

public record DepositTransactionRequest(
        Long amount,
        String referenceCode
) {
    public static DepositTransactionRequest of(Long amount, String referenceCode) {
        return new DepositTransactionRequest(amount, referenceCode);
    }
}

