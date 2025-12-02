package com.ll.deposit.model.vo.response;

import com.ll.deposit.model.entity.Deposit;
import com.ll.deposit.model.enums.DepositStatus;

import java.time.LocalDateTime;

public record DepositResponse (
        String userCode,
        String depositCode,
        Long balance,
        DepositStatus depositStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static DepositResponse from(Deposit deposit) {
        return new DepositResponse(
                deposit.getUserCode(),
                deposit.getCode(),
                deposit.getBalance(),
                deposit.getDepositStatus(),
                deposit.getCreatedAt(),
                deposit.getUpdatedAt()
        );
    }
}
