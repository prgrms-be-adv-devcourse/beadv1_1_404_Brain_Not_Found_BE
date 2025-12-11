package com.ll.core.model.enums;

import lombok.Getter;

@Getter
public enum PaymentRefundNotificationStatus {
    REFUNDED("REFUNDED"),
    REFUND_FAILED("REFUND_FAILED");

    private final String value;

    PaymentRefundNotificationStatus(String value) {
        this.value = value;
    }

    public static PaymentRefundNotificationStatus from(String value) {
        for (PaymentRefundNotificationStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("알 수 없는 환불 알림 상태: " + value);
    }
}

