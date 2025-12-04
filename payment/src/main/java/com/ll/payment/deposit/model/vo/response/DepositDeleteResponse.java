package com.ll.payment.deposit.model.vo.response;

import com.ll.payment.deposit.model.entity.Deposit;
import com.ll.payment.deposit.model.enums.DepositStatus;

import java.time.LocalDateTime;

public record DepositDeleteResponse(
        String userCode,
        String depositCode,
        Long balance,
        DepositStatus depositStatus,
        String closedReason,
        LocalDateTime updatedAt
) {
    public static DepositDeleteResponse from(Deposit deposit, String closedReason) {
        return new DepositDeleteResponse(
                deposit.getUserCode(),
                deposit.getCode(),
                deposit.getBalance(),
                deposit.getDepositStatus(),
                closedReason,
                deposit.getUpdatedAt()
        );
    }
}
