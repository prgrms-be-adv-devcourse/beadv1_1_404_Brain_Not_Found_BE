package com.example.deposit.service;

import com.example.deposit.client.UserServiceClient;
import com.example.deposit.model.entity.Deposit;
import com.example.deposit.model.entity.DepositHistory;
import com.example.deposit.model.enums.DepositHistoryType;
import com.example.deposit.model.enums.TransactionStatus;
import com.example.deposit.model.exception.DepositNotFoundException;
import com.example.deposit.model.exception.DuplicateDepositTransactionException;
import com.example.deposit.model.exception.UserNotFoundException;
import com.example.deposit.model.vo.request.DepositTransactionRequest;
import com.example.deposit.model.vo.response.DepositTransactionResponse;
import com.example.deposit.model.vo.response.DepositResponse;
import com.example.deposit.model.vo.response.UserInfoResponse;
import com.example.deposit.repository.DepositHistoryRepository;
import com.example.deposit.repository.DepositRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            return DepositResponse.from(userCode, getDepositByUserId(userInfo.userId()));
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

        // TODO: 카프카 이벤트 발행 필요한지 검토

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

        // TODO: 카프카 이벤트 발행 필요한지 검토

        return DepositTransactionResponse.from(userCode, depositHistory);
    }

    private Deposit getDepositByUserId(Long userId) {
        return depositRepository.findByUserId(userId)
                .orElseThrow(() -> new DepositNotFoundException("해당 회원의 예치금 계좌가 존재하지 않습니다."));
    }

    private void isDuplicateTransaction(String referenceCode) {
        if (depositHistoryRepository.existsByReferenceCode(referenceCode)) {
            throw new DuplicateDepositTransactionException("이미 처리된 거래요청입니다.");
        }
    }

    private UserInfoResponse getUserInfo(String userCode) {
        UserInfoResponse userInfo = userServiceClient.getUserByCode(userCode);
        if (userInfo == null) {
            throw new UserNotFoundException("해당 유저를 찾을 수 없습니다: " + userCode);
        }
        return userInfo;
    }
}

