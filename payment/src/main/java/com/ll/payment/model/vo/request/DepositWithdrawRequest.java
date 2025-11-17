package com.ll.payment.model.vo.request;

public record DepositWithdrawRequest(
        Integer amount,
        String referenceCode
) {
}

