package com.ll.deposit.model.exception;

import com.ll.core.model.exception.BaseException;

public class DuplicateDepositTransactionException extends BaseException {

    public DuplicateDepositTransactionException() {
        super(ErrorCode.TRANSACTION_ALREADY_EXISTS);
    }

    public DuplicateDepositTransactionException(String customMessage) {
        super(ErrorCode.TRANSACTION_ALREADY_EXISTS, customMessage);
    }
}
