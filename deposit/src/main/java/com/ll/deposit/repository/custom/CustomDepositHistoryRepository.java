package com.ll.deposit.repository.custom;

import com.ll.deposit.model.entity.Deposit;
import com.ll.deposit.model.entity.DepositHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface CustomDepositHistoryRepository {
    Page<DepositHistory> findAllByDepositAndCreatedAtBetween(Deposit deposit, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);
}