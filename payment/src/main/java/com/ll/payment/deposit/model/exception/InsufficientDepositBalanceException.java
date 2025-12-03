package com.ll.payment.deposit.model.exception;

import com.ll.core.model.exception.BaseException;

public class InsufficientDepositBalanceException extends BaseException {

    public InsufficientDepositBalanceException() {
        super(ErrorCode.BALANCE_NOT_ENOUGH);
    }

    public InsufficientDepositBalanceException(String customMessage) {
        super(ErrorCode.BALANCE_NOT_ENOUGH, customMessage);
    }
}
