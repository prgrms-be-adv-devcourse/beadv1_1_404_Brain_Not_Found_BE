package com.example.deposit.service;

import com.example.deposit.model.entity.Deposit;
import com.example.deposit.model.entity.DepositHistory;
import com.example.deposit.model.enums.DepositHistoryType;
import com.example.deposit.model.enums.TransactionStatus;
import com.example.deposit.model.exception.DepositNotFoundException;
import com.example.deposit.model.exception.DuplicateDepositTransactionException;
import com.example.deposit.model.vo.request.DepositDeleteRequest;
import com.example.deposit.model.vo.request.DepositTransactionRequest;
import com.example.deposit.model.vo.response.*;
import com.example.deposit.repository.DepositHistoryRepository;
import com.example.deposit.repository.DepositRepository;
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
        Deposit deposit = getDepositByUserId(userCode);
        return DepositResponse.from(deposit);
    }

    @Override
    public DepositDeleteResponse deleteDepositByUserCode(String userCode, DepositDeleteRequest request) {
        return DepositDeleteResponse.from(depositRepository.save(getDepositByUserId(userCode).setClosed())
                , request.closedReason());
    }

    // TODO: Redis 분산 락 적용 필요
    @Override
    @Transactional
    public DepositTransactionResponse chargeDeposit(String userCode, DepositTransactionRequest request) {
        isDuplicateTransaction(request.referenceCode());

        Deposit deposit = getDepositByUserId(userCode).charge(request.amount());

        DepositHistory depositHistory = depositHistoryRepository.save(DepositHistory.builder()
                .depositId(deposit.getId())
                .amount(request.amount())
                .balanceBefore(deposit.getBalance() - request.amount())
                .balanceAfter(deposit.getBalance())
                .referenceCode(request.referenceCode())
                .historyType(DepositHistoryType.CHARGE)
                .transactionStatus(TransactionStatus.COMPLETED)
                .build()
        );

        // TODO: 카프카 이벤트 발행 필요한지 검토 ( @TransactionalEventListener(phase = AFTER_COMMIT) )

        return DepositTransactionResponse.from(deposit.getCode(), depositHistory);
    }

    // TODO: Redis 분산 락 적용 필요
    @Override
    @Transactional
    public DepositTransactionResponse withdrawDeposit(String userCode, DepositTransactionRequest request) {
        isDuplicateTransaction(request.referenceCode());

        Deposit deposit = getDepositByUserId(userCode).withdraw(request.amount());

        DepositHistory depositHistory = depositHistoryRepository.save(DepositHistory.builder()
                .depositId(deposit.getId())
                .amount(request.amount())
                .balanceBefore(deposit.getBalance() + request.amount())
                .balanceAfter(deposit.getBalance())
                .referenceCode(request.referenceCode())
                .historyType(DepositHistoryType.WITHDRAW)
                .transactionStatus(TransactionStatus.COMPLETED)
                .build()
        );

        // TODO: 카프카 이벤트 발행 필요한지 검토 ( @TransactionalEventListener(phase = AFTER_COMMIT) )

        return DepositTransactionResponse.from(deposit.getCode(), depositHistory);
    }

    @Override
    public DepositHistoryPageResponse getDepositHistoryByUserCode(String userCode, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
        Deposit deposit = getDepositByUserId(userCode);
        Page<DepositHistory> histories = depositHistoryRepository.findAllByDepositAndCreatedAtBetween(deposit, fromDate, toDate, pageable);
        return DepositHistoryPageResponse.from(userCode, histories.map(DepositHistoryResponse::from));
    }

    private Deposit getDepositByUserId(String userCode) {
        return depositRepository.findByUserCode(userCode)
                .orElseThrow(() -> new DepositNotFoundException("해당 회원의 예치금 계좌가 존재하지 않습니다."));
    }

    private void isDuplicateTransaction(String referenceCode) {
        if (depositHistoryRepository.existsByReferenceCode(referenceCode)) {
            throw new DuplicateDepositTransactionException("이미 처리된 거래요청입니다.");
        }
    }
}