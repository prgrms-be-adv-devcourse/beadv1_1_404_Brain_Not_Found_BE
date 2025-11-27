package com.ll.deposit.model.exception;

import com.ll.core.model.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode implements BaseErrorCode {
    DEPOSIT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 입금 계좌를 찾을 수 없습니다."),
    DEPOSIT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 입금 계좌입니다."),
    DEPOSIT_ALREADY_CLOSED(HttpStatus.CONFLICT, "이미 비활성 상태인 입금 계좌입니다."),
    INVALID_DEPOSIT_HISTORY_TYPE(HttpStatus.UNPROCESSABLE_ENTITY, "유효하지 않은 거래 유형입니다."),
    BALANCE_NOT_ENOUGH(HttpStatus.UNPROCESSABLE_ENTITY, "계좌의 잔액이 부족합니다."),
    CAN_NOT_TRANSACT_ON_INACTIVE_DEPOSIT(HttpStatus.UNPROCESSABLE_ENTITY, "비활성 상태인 입금 계좌에는 거래를 할 수 없습니다."),
    TRANSACTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 처리된 거래요청입니다."),
    REFUND_TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "환불 대상 거래 이력을 찾을 수 없습니다."),
    BALANCE_NOT_EMPTY(HttpStatus.UNPROCESSABLE_ENTITY, "잔액이 남아있는 입금 계좌는 삭제할 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
