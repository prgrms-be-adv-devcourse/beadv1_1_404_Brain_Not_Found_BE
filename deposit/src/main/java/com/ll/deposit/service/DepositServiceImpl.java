package com.ll.deposit.service;

import com.ll.deposit.model.entity.Deposit;
import com.ll.deposit.model.entity.DepositHistory;
import com.ll.deposit.model.exception.DepositNotFoundException;
import com.ll.deposit.model.exception.DuplicateDepositTransactionException;
import com.ll.deposit.model.exception.RefundTargetNotFoundException;
import com.ll.deposit.model.vo.request.DepositDeleteRequest;
import com.ll.deposit.model.vo.request.DepositTransactionRequest;
import com.ll.deposit.model.vo.response.*;
import com.ll.deposit.repository.DepositHistoryRepository;
import com.ll.deposit.repository.DepositRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositServiceImpl implements DepositService {
    public static final String REFUND = "Refund-";
    private final DepositRepository depositRepository;
    private final DepositHistoryRepository depositHistoryRepository;

    @Override
    public DepositResponse createDeposit(String userCode) {
        return DepositResponse.from(depositRepository.findByUserCode(userCode)
                .map(exit -> depositRepository.save(exit.setActive()))
                .orElseGet(() -> depositRepository.save(Deposit.createInitialDeposit(userCode))));
    }

    @Override
    public DepositResponse getDepositByUserCode(String userCode) {
        return DepositResponse.from(findDepositByUserCode(userCode));
    }

    @Override
    public DepositDeleteResponse deleteDepositByUserCode(String userCode, DepositDeleteRequest request) {
        Deposit deposit = findDepositByUserCode(userCode);
        Deposit saved = depositRepository.save(deposit.setClosed());
        return DepositDeleteResponse.from(saved, request.closedReason());
    }

    // TODO: 트랜잭션 처리 로직 개선 필요
    @Override
    @Transactional
    public DepositTransactionResponse chargeDeposit(String userCode, DepositTransactionRequest request) {
        isDuplicateTransaction(request.referenceCode());
        Deposit deposit = findDepositByUserCode(userCode);
        DepositHistory history = deposit.charge(request.amount(), request.referenceCode());
        return DepositTransactionResponse.from(deposit.getCode(), depositHistoryRepository.save(history));
    }

    @Override
    @Transactional
    public DepositTransactionResponse withdrawDeposit(String userCode, DepositTransactionRequest request) {
        isDuplicateTransaction(request.referenceCode());
        Deposit deposit = findDepositByUserCode(userCode);
        DepositHistory history = deposit.withdraw(request.amount(), request.referenceCode());
        return DepositTransactionResponse.from(deposit.getCode(), depositHistoryRepository.save(history));
    }

    @Override
    @Transactional
    public DepositTransactionResponse paymentDeposit(String userCode, DepositTransactionRequest request) {
        isDuplicateTransaction(request.referenceCode());
        Deposit deposit = findDepositByUserCode(userCode);
        DepositHistory history = deposit.payment(request.amount(), request.referenceCode());
        return DepositTransactionResponse.from(deposit.getCode(), depositHistoryRepository.save(history));
    }

    @Override
    @Transactional
    public DepositTransactionResponse refundDeposit(String userCode, DepositTransactionRequest request) {
        isDuplicateTransactionForRefund(request.referenceCode());
        Deposit deposit = findDepositByUserCode(userCode);
        DepositHistory history = deposit.refund(request.amount(), request.referenceCode());
        return DepositTransactionResponse.from(deposit.getCode(), depositHistoryRepository.save(history));
    }

    @Override
    public DepositHistoryPageResponse getDepositHistoryByUserCode(String userCode, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
        Deposit deposit = findDepositByUserCode(userCode);
        Page<DepositHistory> histories = depositHistoryRepository.findAllByDepositAndCreatedAtBetween(deposit, fromDate, toDate, pageable);
        return DepositHistoryPageResponse.from(userCode, histories.map(DepositHistoryResponse::from));
    }

    private Deposit findDepositByUserCode(String userCode) {
        return depositRepository.findByUserCode(userCode)
                .orElseThrow(DepositNotFoundException::new);
    }

    private void isDuplicateTransaction(String referenceCode) {
        if (depositHistoryRepository.existsByReferenceCode(referenceCode)) {
            throw new DuplicateDepositTransactionException();
        }
    }

    private void isDuplicateTransactionForRefund(String referenceCode) {
        isDuplicateTransaction(refundCode(referenceCode));
        if (!depositHistoryRepository.existsByReferenceCode(referenceCode)) {
            throw new RefundTargetNotFoundException();
        }
    }

    public static String refundCode(String ref) {
        return REFUND + ref;
    }
}