package com.ll.payment.deposit.model.vo.response;

import com.ll.payment.deposit.model.entity.DepositHistory;
import com.ll.payment.deposit.model.enums.DepositHistoryType;

import java.time.LocalDateTime;

public record DepositHistoryResponse (
        String depositHistoryCode,
        Long amount,
        Long balanceBefore,
        Long balanceAfter,
        DepositHistoryType historyType,
        String referenceCode,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static DepositHistoryResponse from(DepositHistory depositHistory) {
        return new DepositHistoryResponse(
                depositHistory.getCode(),
                depositHistory.getAmount(),
                depositHistory.getBalanceBefore(),
                depositHistory.getBalanceAfter(),
                depositHistory.getHistoryType(),
                depositHistory.getReferenceCode(),
                depositHistory.getCreatedAt(),
                depositHistory.getUpdatedAt()
        );
    }
}