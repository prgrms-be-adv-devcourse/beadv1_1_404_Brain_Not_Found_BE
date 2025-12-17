package com.ll.payment.settlement.model.exception;

import com.ll.core.model.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode implements BaseErrorCode {
    SETTLEMENT_INVALID_STATE_TRANSITION(HttpStatus.CONFLICT, "이미 처리가 완료된 정산 건입니다."),
    SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 정산을 찾을 수 없습니다."),
    REFUND_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, "환불 가능 기간이 만료되었습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
