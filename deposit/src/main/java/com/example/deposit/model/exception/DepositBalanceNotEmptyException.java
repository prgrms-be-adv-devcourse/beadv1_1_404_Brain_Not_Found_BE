package com.example.deposit.model.exception;

import com.example.core.exception.BaseException;
import com.example.core.exception.ErrorCode;

public class DepositBalanceNotEmptyException extends BaseException {

    public DepositBalanceNotEmptyException() {
        super(ErrorCode.BALANCE_NOT_EMPTY);
    }

    public DepositBalanceNotEmptyException(String customMessage) {
        super(ErrorCode.BALANCE_NOT_EMPTY, customMessage);
    }
}
