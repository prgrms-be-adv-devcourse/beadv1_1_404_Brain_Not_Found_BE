package com.ll.payment.deposit.model.exception;

import com.ll.core.model.exception.BaseException;

public class DepositAmountMismatchException extends BaseException {

    public DepositAmountMismatchException() {
        super(ErrorCode.DEPOSIT_AMOUNT_MISMATCH);
    }

    public DepositAmountMismatchException(String customMessage) {
        super(ErrorCode.DEPOSIT_AMOUNT_MISMATCH, customMessage);
    }
}
