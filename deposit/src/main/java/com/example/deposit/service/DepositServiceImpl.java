package com.example.deposit.service;

import com.example.deposit.model.entity.Deposit;
import com.example.deposit.model.entity.DepositHistory;
import com.example.deposit.model.enums.DepositHistoryType;
import com.example.deposit.model.enums.DepositStatus;
import com.example.deposit.model.enums.TransactionStatus;
import com.example.deposit.model.exception.DepositAlreadyExistsException;
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
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositServiceImpl implements DepositService {
    private final DepositRepository depositRepository;
    private final DepositHistoryRepository depositHistoryRepository;

    @Override
    @Transactional
    public DepositResponse createDeposit(String userCode) {
        Optional<Deposit> optional = depositRepository.findByUserCode(userCode);
        if (optional.isPresent()) {
            Deposit d = optional.get();
            if (d.getDepositStatus() == DepositStatus.ACTIVE) {
                throw new DepositAlreadyExistsException();
            }
            log.warn("Closed deposit found for user {}. Reactivating...", userCode);
            d.setActive();
            return DepositResponse.from(userCode, d);
        }
        Deposit deposit = depositRepository.save(Deposit.createInitialDeposit(userCode));
        return DepositResponse.from(userCode, deposit);
    }

    @Override
    @Transactional(readOnly = true)
    public DepositResponse getDepositByUserCode(String userCode) {
        Deposit deposit = getDepositByUserId(userCode);
        return DepositResponse.from(userCode, deposit);
    }

    @Override
    @Transactional
    public DepositDeleteResponse deleteDepositByUserCode(String userCode, DepositDeleteRequest request) {
        Deposit deposit = getDepositByUserId(userCode);
        deposit.setClosed();
        return DepositDeleteResponse.from(userCode, deposit, request.closedReason());
    }

    // TODO: Redis 분산 락 적용 필요
    @Override
    @Transactional
    public DepositTransactionResponse chargeDeposit(String userCode, DepositTransactionRequest request) {
        Deposit deposit = getDepositByUserId(userCode);
        isDuplicateTransaction(request.referenceCode());

        deposit.charge(request.amount());

        DepositHistory depositHistory = DepositHistory.builder()
                .deposit(deposit)
                .amount(request.amount())
                .balanceBefore(deposit.getBalance() - request.amount())
                .balanceAfter(deposit.getBalance())
                .referenceCode(request.referenceCode())
                .historyType(DepositHistoryType.CHARGE)
                .transactionStatus(TransactionStatus.COMPLETED)
                .build();

        deposit.addHistory(depositHistory);
        depositHistoryRepository.save(depositHistory);

        // TODO: 카프카 이벤트 발행 필요한지 검토 ( @TransactionalEventListener(phase = AFTER_COMMIT) )

        return DepositTransactionResponse.from(userCode, depositHistory);
    }

    // TODO: Redis 분산 락 적용 필요
    @Override
    @Transactional
    public DepositTransactionResponse withdrawDeposit(String userCode, DepositTransactionRequest request) {
        Deposit deposit = getDepositByUserId(userCode);
        isDuplicateTransaction(request.referenceCode());

        deposit.withdraw(request.amount());

        DepositHistory depositHistory = DepositHistory.builder()
                .deposit(deposit)
                .amount(request.amount())
                .balanceBefore(deposit.getBalance() + request.amount())
                .balanceAfter(deposit.getBalance())
                .referenceCode(request.referenceCode())
                .historyType(DepositHistoryType.WITHDRAW)
                .transactionStatus(TransactionStatus.COMPLETED)
                .build();

        deposit.addHistory(depositHistory);
        depositHistoryRepository.save(depositHistory);

        // TODO: 카프카 이벤트 발행 필요한지 검토 ( @TransactionalEventListener(phase = AFTER_COMMIT) )

        return DepositTransactionResponse.from(userCode, depositHistory);
    }

    @Override
    @Transactional(readOnly = true)
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

