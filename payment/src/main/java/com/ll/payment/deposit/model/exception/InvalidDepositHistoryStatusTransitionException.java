package com.ll.payment.deposit.model.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class InvalidDepositHistoryStatusTransitionException extends BaseException {

    public InvalidDepositHistoryStatusTransitionException() {
        super(ErrorCode.CONFLICT);
    }

    public InvalidDepositHistoryStatusTransitionException(String customMessage) {
        super(ErrorCode.CONFLICT, customMessage);
    }
}
