package com.example.deposit.model.exception;

import com.example.core.model.exception.BaseException;
import com.example.core.model.exception.ErrorCode;

public class InvalidDepositHistoryStatusTransitionException extends BaseException {

    public InvalidDepositHistoryStatusTransitionException() {
        super(ErrorCode.CONFLICT);
    }

    public InvalidDepositHistoryStatusTransitionException(String customMessage) {
        super(ErrorCode.CONFLICT, customMessage);
    }
}
