package com.ll.deposit.model.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class DuplicateDepositTransactionException extends BaseException {

    public DuplicateDepositTransactionException() {
        super(ErrorCode.CONFLICT);
    }

    public DuplicateDepositTransactionException(String customMessage) {
        super(ErrorCode.CONFLICT, customMessage);
    }
}
