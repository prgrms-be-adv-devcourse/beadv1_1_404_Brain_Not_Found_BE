package com.example.deposit.service;

import com.example.deposit.client.UserServiceClient;
import com.example.deposit.model.entity.Deposit;
import com.example.deposit.model.entity.DepositHistory;
import com.example.deposit.model.enums.DepositHistoryType;
import com.example.deposit.model.enums.TransactionStatus;
import com.example.deposit.model.exception.DepositAlreadyExistsException;
import com.example.deposit.model.exception.DepositNotFoundException;
import com.example.deposit.model.exception.DuplicateDepositTransactionException;
import com.example.deposit.model.exception.UserNotFoundException;
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
    private final UserServiceClient userServiceClient;

    @Override
    @Transactional
    public DepositResponse createDeposit(String userCode) {
        UserInfoResponse userInfo = getUserInfo(userCode);
        if ( depositRepository.existsByUserId(userInfo.userId()) ) {
            log.warn("User {} already exists", userCode);
            throw new DepositAlreadyExistsException();
        }
        Deposit deposit = Deposit.createInitialDeposit(userInfo.userId());
        depositRepository.save(deposit);
        return DepositResponse.from(userCode, deposit);
    }

    @Override
    @Transactional(readOnly = true)
    public DepositResponse getDepositByUserCode(String userCode) {
        UserInfoResponse userInfo = getUserInfo(userCode);
        Deposit deposit = getDepositByUserId(userInfo.userId());
        return DepositResponse.from(userCode, deposit);
    }

    @Override
    @Transactional
    public DepositDeleteResponse deleteDepositByUserCode(String userCode, DepositDeleteRequest request) {
        UserInfoResponse userInfo = getUserInfo(userCode);
        Deposit deposit = getDepositByUserId(userInfo.userId());
        deposit.setClosed();
        return DepositDeleteResponse.from(userCode, deposit, request.closedReason());
    }

    // TODO: Redis 분산 락 적용 필요
    @Override
    @Transactional
    public DepositTransactionResponse chargeDeposit(String userCode, DepositTransactionRequest request) {
        UserInfoResponse userInfo = getUserInfo(userCode);
        Deposit deposit = getDepositByUserId(userInfo.userId());
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

        depositHistoryRepository.save(depositHistory);
        deposit.addHistory(depositHistory);

        // TODO: 카프카 이벤트 발행 필요한지 검토 ( @TransactionalEventListener(phase = AFTER_COMMIT) )

        return DepositTransactionResponse.from(userCode, depositHistory);
    }

    // TODO: Redis 분산 락 적용 필요
    @Override
    @Transactional
    public DepositTransactionResponse withdrawDeposit(String userCode, DepositTransactionRequest request) {
        UserInfoResponse userInfo = getUserInfo(userCode);
        Deposit deposit = getDepositByUserId(userInfo.userId());
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

        depositHistoryRepository.save(depositHistory);
        deposit.addHistory(depositHistory);

        // TODO: 카프카 이벤트 발행 필요한지 검토 ( @TransactionalEventListener(phase = AFTER_COMMIT) )

        return DepositTransactionResponse.from(userCode, depositHistory);
    }

    @Override
    @Transactional(readOnly = true)
    public DepositHistoryPageResponse getDepositHistoryByUserCode(String userCode, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
        UserInfoResponse userInfo = getUserInfo(userCode);
        Deposit deposit = getDepositByUserId(userInfo.userId());

        System.out.println("fromDate = " + fromDate);
        System.out.println("toDate = " + toDate);

        Page<DepositHistory> histories = depositHistoryRepository.findAllByDepositAndCreatedAtBetween(deposit, fromDate, toDate, pageable);

        return DepositHistoryPageResponse.from(userCode, histories.map(DepositHistoryResponse::from));
    }

    private Deposit getDepositByUserId(Long userId) {
        return depositRepository.findByUserId(userId)
                .orElseThrow(() -> new DepositNotFoundException("해당 회원의 예치금 계좌가 존재하지 않습니다."));
    }

    private UserInfoResponse getUserInfo(String userCode) {
        UserInfoResponse userInfo = userServiceClient.getUserByCode(userCode);
        if (userInfo == null) {
            throw new UserNotFoundException("해당 유저를 찾을 수 없습니다: " + userCode);
        }
        return userInfo;
    }

    private void isDuplicateTransaction(String referenceCode) {
        if (depositHistoryRepository.existsByReferenceCode(referenceCode)) {
            throw new DuplicateDepositTransactionException("이미 처리된 거래요청입니다.");
        }
    }
}

