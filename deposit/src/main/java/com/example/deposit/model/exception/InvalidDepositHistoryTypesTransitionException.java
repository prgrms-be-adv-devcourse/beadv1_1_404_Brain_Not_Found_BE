package com.example.deposit.model.exception;

import com.example.core.exception.BaseException;
import com.example.core.exception.ErrorCode;

public class InvalidDepositHistoryTypesTransitionException extends BaseException {

    public InvalidDepositHistoryTypesTransitionException() {
        super(ErrorCode.CONFLICT);
    }

    public InvalidDepositHistoryTypesTransitionException(String customMessage) {
        super(ErrorCode.CONFLICT, customMessage);
    }
}
