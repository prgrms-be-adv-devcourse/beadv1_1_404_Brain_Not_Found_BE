package com.ll.deposit.model.vo.response;

import com.ll.deposit.model.entity.DepositHistory;
import com.ll.deposit.model.enums.DepositHistoryType;
import com.ll.deposit.model.enums.TransactionStatus;

import java.time.LocalDateTime;

public record DepositHistoryResponse (
        String depositHistoryCode,
        Long amount,
        Long balanceBefore,
        Long balanceAfter,
        DepositHistoryType historyType,
        TransactionStatus transactionStatus,
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
                depositHistory.getTransactionStatus(),
                depositHistory.getReferenceCode(),
                depositHistory.getCreatedAt(),
                depositHistory.getUpdatedAt()
        );
    }
}