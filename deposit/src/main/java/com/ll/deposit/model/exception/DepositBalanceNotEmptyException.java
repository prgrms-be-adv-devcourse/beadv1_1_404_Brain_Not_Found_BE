package com.ll.deposit.model.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class DepositBalanceNotEmptyException extends BaseException {

    public DepositBalanceNotEmptyException() {
        super(ErrorCode.BALANCE_NOT_EMPTY);
    }

    public DepositBalanceNotEmptyException(String customMessage) {
        super(ErrorCode.BALANCE_NOT_EMPTY, customMessage);
    }
}
