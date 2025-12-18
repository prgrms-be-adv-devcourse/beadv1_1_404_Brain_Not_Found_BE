package com.ll.payment.payment.model.enums;

public enum PaymentOutboxStatus {
    PENDING,    // 발행 대기
    PUBLISHED,  // 발행 완료
    FAILED      // 발행 실패 (재시도 한계 초과)
}

