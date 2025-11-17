package com.ll.deposit.model.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class InsufficientDepositBalanceException extends BaseException {

    public InsufficientDepositBalanceException() {
        super(ErrorCode.UNPROCESSABLE_ENTITY);
    }

    public InsufficientDepositBalanceException(String customMessage) {
        super(ErrorCode.UNPROCESSABLE_ENTITY, customMessage);
    }
}
