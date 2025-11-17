package com.ll.settlement.model.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class SettlementNotFoundException extends BaseException {

    public SettlementNotFoundException() {
        super(ErrorCode.SETTLEMENT_NOT_FOUND);
    }

    public SettlementNotFoundException(String customMessage) {
        super(ErrorCode.SETTLEMENT_NOT_FOUND, customMessage);
    }
}
