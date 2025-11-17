package com.ll.deposit.model.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class DepositAlreadyExistsException extends BaseException {

    public DepositAlreadyExistsException() {
        super(ErrorCode.DEPOSIT_ALREADY_EXISTS);
    }

    public DepositAlreadyExistsException(String customMessage) {
        super(ErrorCode.DEPOSIT_ALREADY_EXISTS, customMessage);
    }
}
