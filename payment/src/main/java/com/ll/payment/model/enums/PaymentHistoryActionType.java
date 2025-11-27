package com.ll.payment.model.enums;

public enum PaymentHistoryActionType {
    CREATE,           // 결제 생성
    COMPLETE,         // 결제 완료
    REFUND,           // 환불
    CHARGE,           // 충전
    STATUS_CHANGE     // 상태 변경
}

