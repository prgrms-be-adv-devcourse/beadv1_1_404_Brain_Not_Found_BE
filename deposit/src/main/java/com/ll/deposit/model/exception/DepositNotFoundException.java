package com.ll.deposit.model.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class DepositNotFoundException extends BaseException {

    public DepositNotFoundException() {
        super(ErrorCode.NOT_FOUND);
    }

    public DepositNotFoundException(String customMessage) {
        super(ErrorCode.NOT_FOUND, customMessage);
    }
}
