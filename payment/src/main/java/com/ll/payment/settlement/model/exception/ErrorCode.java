package com.ll.payment.settlement.model.exception;

import com.ll.core.model.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode implements BaseErrorCode {
    SETTLEMENT_INVALID_STATE_TRANSITION(HttpStatus.CONFLICT, "정산의 상태 전이가 유효하지 않습니다."),
    SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 정산을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
