package com.example.deposit.model.exception;

import com.example.core.model.exception.BaseException;
import com.example.core.model.exception.ErrorCode;

public class InvalidDepositStatusTransitionException extends BaseException {

    public InvalidDepositStatusTransitionException() {
        super(ErrorCode.DEPOSIT_ALREADY_CLOSED);
    }

    public InvalidDepositStatusTransitionException(String customMessage) {
        super(ErrorCode.DEPOSIT_ALREADY_CLOSED, customMessage);
    }
}
