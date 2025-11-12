package com.example.deposit.model.exception;

import com.example.core.exception.BaseException;
import com.example.core.exception.ErrorCode;

public class DepositAlreadyExistsException extends BaseException {

    public DepositAlreadyExistsException() {
        super(ErrorCode.DEPOSIT_ALREADY_EXISTS);
    }

    public DepositAlreadyExistsException(String customMessage) {
        super(ErrorCode.DEPOSIT_ALREADY_EXISTS, customMessage);
    }
}
