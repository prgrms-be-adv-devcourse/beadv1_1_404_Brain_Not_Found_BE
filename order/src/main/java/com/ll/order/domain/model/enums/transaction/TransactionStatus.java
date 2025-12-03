package com.ll.order.domain.model.enums.transaction;

public enum TransactionStatus {
    IN_PROGRESS,    // 진행 중
    COMPLETED,      // 완료
    FAILED,         // 실패
    COMPENSATING    // 보상 중
}

