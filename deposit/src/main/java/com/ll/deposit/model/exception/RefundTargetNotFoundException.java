package com.ll.deposit.model.exception;

import com.ll.core.model.exception.BaseException;

public class RefundTargetNotFoundException extends BaseException {

    public RefundTargetNotFoundException() {
        super(ErrorCode.REFUND_TRANSACTION_NOT_FOUND);
    }

    public RefundTargetNotFoundException(String customMessage) {
        super(ErrorCode.REFUND_TRANSACTION_NOT_FOUND, customMessage);
    }
}
