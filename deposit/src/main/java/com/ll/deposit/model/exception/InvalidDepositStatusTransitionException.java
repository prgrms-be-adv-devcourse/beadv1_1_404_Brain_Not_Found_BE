package com.ll.deposit.model.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class InvalidDepositStatusTransitionException extends BaseException {

    public InvalidDepositStatusTransitionException() {
        super(ErrorCode.DEPOSIT_ALREADY_CLOSED);
    }

    public InvalidDepositStatusTransitionException(String customMessage) {
        super(ErrorCode.DEPOSIT_ALREADY_CLOSED, customMessage);
    }
}
