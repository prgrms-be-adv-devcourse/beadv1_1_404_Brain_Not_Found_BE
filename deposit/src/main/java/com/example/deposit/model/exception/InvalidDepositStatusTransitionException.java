package com.example.deposit.model.exception;

import com.example.core.exception.BaseException;
import com.example.core.exception.ErrorCode;

public class InvalidDepositStatusTransitionException extends BaseException {

    public InvalidDepositStatusTransitionException() {
        super(ErrorCode.CONFLICT);
    }

    public InvalidDepositStatusTransitionException(String customMessage) {
        super(ErrorCode.CONFLICT, customMessage);
    }
}
