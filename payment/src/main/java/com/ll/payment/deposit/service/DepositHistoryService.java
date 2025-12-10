package com.ll.payment.deposit.service;

import com.ll.payment.deposit.model.entity.Deposit;
import com.ll.payment.deposit.model.entity.DepositHistory;
import com.ll.payment.deposit.model.enums.DepositHistoryType;
import com.ll.payment.deposit.model.vo.request.DepositTransactionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface DepositHistoryService {
    Page<DepositHistory> getDepositHistory(Deposit deposit, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);
    void saveSuccessHistory(DepositHistory history);
    void saveFailedHistory(Deposit deposit, DepositTransactionRequest request, DepositHistoryType type, Exception e);
    void validateDuplicate(String referenceCode);
    void validateDuplicateForRefund(String referenceCode);
}
