package com.example.deposit.model.exception;

import com.example.core.model.exception.BaseException;
import com.example.core.model.exception.ErrorCode;

public class InsufficientDepositBalanceException extends BaseException {

    public InsufficientDepositBalanceException() {
        super(ErrorCode.UNPROCESSABLE_ENTITY);
    }

    public InsufficientDepositBalanceException(String customMessage) {
        super(ErrorCode.UNPROCESSABLE_ENTITY, customMessage);
    }
}
