package com.ll.payment.model.vo;

public record PaymentRequest(
        String userCode,
        String orderCode,
        boolean useDeposit,
        Long depositAmount,
        Long paymentAmount,
        String paymentKey
) {
}
