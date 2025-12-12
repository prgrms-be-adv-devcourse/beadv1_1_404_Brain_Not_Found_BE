package com.ll.order.domain.model.vo;

import jakarta.validation.constraints.NotBlank;

public record PaymentRefundNotificationEvent(
        @NotBlank(message = "orderCode는 공백이거나 null일 수 없습니다.")
        String orderCode,
        
        @NotBlank(message = "status는 공백이거나 null일 수 없습니다.")
        String status
) {
    public static PaymentRefundNotificationEvent of(String orderCode, String status) {
        return new PaymentRefundNotificationEvent(orderCode, status);
    }
}

