package com.ll.deposit.model.exception;

import com.ll.core.model.exception.BaseException;

public class UnduplicateDepositTransactionException extends BaseException {

    public UnduplicateDepositTransactionException() {
        super(ErrorCode.REFUND_TRANSACTION_NOT_FOUND);
    }

    public UnduplicateDepositTransactionException(String customMessage) {
        super(ErrorCode.REFUND_TRANSACTION_NOT_FOUND, customMessage);
    }
}
