package com.ll.payment.settlement.model.exception;

import com.ll.core.model.exception.BaseException;

public class SettlementNotFoundException extends BaseException {

    public SettlementNotFoundException() {
        super(ErrorCode.SETTLEMENT_NOT_FOUND);
    }

    public SettlementNotFoundException(String customMessage) {
        super(ErrorCode.SETTLEMENT_NOT_FOUND, customMessage);
    }
}
