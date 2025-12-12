package com.ll.payment.deposit.service;

import com.ll.payment.deposit.model.entity.Deposit;
import com.ll.payment.deposit.model.entity.DepositHistory;
import com.ll.payment.deposit.model.enums.DepositHistoryType;
import com.ll.payment.deposit.model.exception.DuplicateDepositTransactionException;
import com.ll.payment.deposit.model.exception.RefundTargetNotFoundException;
import com.ll.payment.deposit.model.vo.request.DepositTransactionRequest;
import com.ll.payment.deposit.repository.DepositHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DepositHistoryServiceImpl implements DepositHistoryService {
    public static final String REFUND = "Refund-";
    private final DepositHistoryRepository depositHistoryRepository;

    @Override
    public Page<DepositHistory> getDepositHistory(Deposit deposit, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
        return depositHistoryRepository.findAllByDepositAndCreatedAtBetween(deposit, fromDate, toDate, pageable);
    }

    @Override
    public void saveSuccessHistory(DepositHistory history) {
        depositHistoryRepository.save(history);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailedHistory(Deposit deposit, DepositTransactionRequest request, DepositHistoryType type, Exception e) {
        DepositHistory failedHistory = DepositHistory.createFailedHistory(
                deposit.getId(),
                request.amount(),
                deposit.getBalance(),
                deposit.getBalance(),
                request.referenceCode(),
                type, e);
        depositHistoryRepository.save(failedHistory);
    }

    @Override
    public void validateDuplicate(String referenceCode) {
        if (depositHistoryRepository.existsByReferenceCode(referenceCode)) {
            throw new DuplicateDepositTransactionException();
        }
    }

    @Override
    public void validateDuplicateForRefund(String referenceCode) {
        validateDuplicate(refundCode(referenceCode));
        if (!depositHistoryRepository.existsByReferenceCode(referenceCode)) {
            throw new RefundTargetNotFoundException();
        }
    }

    public static String refundCode(String ref) {
        return REFUND + ref;
    }

}
