package com.ll.payment.payment.model.vo.request;

public record DepositWithdrawRequest(
        Integer amount,
        String referenceCode
) {
}

