package com.ll.core.model.vo.kafka;

import jakarta.validation.constraints.*;

public record PaymentRefundRequestEvent(
        @NotNull(message = "orderId 는 필수입력값입니다.")
        Long orderId,
        @NotBlank(message = "orderCode 는 공백이거나 null일 수 없습니다.")
        String orderCode,
        @NotBlank(message = "buyerCode 는 공백이거나 null일 수 없습니다.")
        String buyerCode,
        @NotNull(message = "refundAmount 는 필수입력값입니다.")
        @Positive(message = "refundAmount 는 0보다 커야 합니다.")
        Integer refundAmount,
        String reason
) {
    public static PaymentRefundRequestEvent from(Long orderId, String orderCode, String buyerCode, Integer refundAmount, String reason) {
        return new PaymentRefundRequestEvent(orderId, orderCode, buyerCode, refundAmount, reason);
    }
}

