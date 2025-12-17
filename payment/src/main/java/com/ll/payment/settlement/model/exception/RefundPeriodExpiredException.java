package com.ll.payment.settlement.model.exception;

import com.ll.core.model.exception.BaseException;

public class RefundPeriodExpiredException extends BaseException {

    public RefundPeriodExpiredException() {
        super(ErrorCode.REFUND_PERIOD_EXPIRED);
    }

    public RefundPeriodExpiredException(String customMessage) {
        super(ErrorCode.REFUND_PERIOD_EXPIRED, customMessage);
    }
}
