package com.example.deposit.model.exception;

import com.example.core.exception.BaseException;
import com.example.core.exception.ErrorCode;

public class InvalidDepositAmountException extends BaseException {

    public InvalidDepositAmountException() {
        super(ErrorCode.BAD_REQUEST);
    }

    public InvalidDepositAmountException(String customMessage) {
        super(ErrorCode.BAD_REQUEST, customMessage);
    }
}
