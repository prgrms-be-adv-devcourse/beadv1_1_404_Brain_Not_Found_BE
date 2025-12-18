package com.ll.payment.deposit.service;

import com.ll.payment.deposit.model.entity.Deposit;
import com.ll.payment.deposit.model.entity.DepositHistory;
import com.ll.payment.deposit.model.enums.DepositHistoryType;
import com.ll.payment.deposit.model.exception.DepositAmountMismatchException;
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
import java.util.Objects;

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
    public void validateDuplicate(DepositTransactionRequest request) {
        if (depositHistoryRepository.existsByReferenceCode(request.referenceCode())) {
            throw new DuplicateDepositTransactionException();
        }
    }

    @Override
    public void validateDuplicateForRefund(DepositTransactionRequest request) {
        validateDuplicate(new DepositTransactionRequest(request.amount(), refundCode(request.referenceCode())));
        DepositHistory depositHistory = depositHistoryRepository.findByReferenceCode(request.referenceCode())
                .orElseThrow(RefundTargetNotFoundException::new);
        if (depositHistory.getHistoryType() != DepositHistoryType.PAYMENT) {
            throw new RefundTargetNotFoundException();
        }
        if (!Objects.equals(depositHistory.getAmount(), request.amount())) {
            throw new DepositAmountMismatchException();
        }
    }

    public static String refundCode(String ref) {
        return REFUND + ref;
    }

}
