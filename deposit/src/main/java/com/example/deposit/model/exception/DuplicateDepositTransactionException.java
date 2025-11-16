package com.example.deposit.model.exception;

import com.example.core.model.exception.BaseException;
import com.example.core.model.exception.ErrorCode;

public class DuplicateDepositTransactionException extends BaseException {

    public DuplicateDepositTransactionException() {
        super(ErrorCode.CONFLICT);
    }

    public DuplicateDepositTransactionException(String customMessage) {
        super(ErrorCode.CONFLICT, customMessage);
    }
}
