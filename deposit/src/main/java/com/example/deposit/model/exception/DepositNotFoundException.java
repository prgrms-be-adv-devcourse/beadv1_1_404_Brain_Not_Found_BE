package com.example.deposit.model.exception;

import com.example.core.model.exception.BaseException;
import com.example.core.model.exception.ErrorCode;

public class DepositNotFoundException extends BaseException {

    public DepositNotFoundException() {
        super(ErrorCode.NOT_FOUND);
    }

    public DepositNotFoundException(String customMessage) {
        super(ErrorCode.NOT_FOUND, customMessage);
    }
}
