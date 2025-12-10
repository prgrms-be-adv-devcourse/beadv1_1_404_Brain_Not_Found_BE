package com.ll.payment.settlement.model.vo;

import com.ll.payment.deposit.model.vo.request.DepositTransactionRequest;
import com.ll.payment.settlement.model.entity.Settlement;

public record SettlementProcessDTO (
        Settlement settlement,
        DepositTransactionRequest request
) {
}
