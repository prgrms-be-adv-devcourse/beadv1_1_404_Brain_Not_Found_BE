package com.ll.payment.deposit.model.exception;

import com.ll.core.model.exception.BaseException;

public class DepositNotFoundException extends BaseException {

    public DepositNotFoundException() {
        super(ErrorCode.DEPOSIT_NOT_FOUND);
    }

    public DepositNotFoundException(String customMessage) {
        super(ErrorCode.DEPOSIT_NOT_FOUND, customMessage);
    }
}
