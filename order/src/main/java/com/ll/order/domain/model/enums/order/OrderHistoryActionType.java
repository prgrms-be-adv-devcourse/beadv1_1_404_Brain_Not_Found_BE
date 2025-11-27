package com.ll.order.domain.model.enums.order;

public enum OrderHistoryActionType {
    CREATE,              // 주문 생성
    STATUS_CHANGE,       // 상태 변경
    CANCEL,              // 주문 취소
    REFUND,              // 환불
    PAYMENT_COMPLETE     // 결제 완료
}

