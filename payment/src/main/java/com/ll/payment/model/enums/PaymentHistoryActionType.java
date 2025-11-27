package com.ll.payment.model.enums;

public enum PaymentHistoryActionType {
    REQUEST,          // 결제 요청 (PG에 결제 요청)
    SUCCESS,          // 결제 성공 (PG 승인 완료)
    FAIL,             // 결제 실패
    CANCEL,           // 결제 취소
    REFUND_REQUEST,   // 환불 요청
    REFUND_DONE,      // 환불 완료
    CHARGE,           // 충전 (예치금 충전용)
    STATUS_CHANGE     // 상태 변경 (기타 상태 변경)
}

