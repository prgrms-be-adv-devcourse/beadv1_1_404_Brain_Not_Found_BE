package com.ll.payment.deposit.model.exception;

import com.ll.core.model.exception.BaseException;

public class DepositBalanceNotEmptyException extends BaseException {

    public DepositBalanceNotEmptyException() {
        super(ErrorCode.BALANCE_NOT_EMPTY);
    }

    public DepositBalanceNotEmptyException(String customMessage) {
        super(ErrorCode.BALANCE_NOT_EMPTY, customMessage);
    }
}
