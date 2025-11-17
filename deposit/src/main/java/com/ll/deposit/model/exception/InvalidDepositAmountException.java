package com.ll.deposit.model.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class InvalidDepositAmountException extends BaseException {

    public InvalidDepositAmountException() {
        super(ErrorCode.BAD_REQUEST);
    }

    public InvalidDepositAmountException(String customMessage) {
        super(ErrorCode.BAD_REQUEST, customMessage);
    }
}
