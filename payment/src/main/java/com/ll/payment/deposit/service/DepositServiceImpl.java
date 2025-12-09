package com.ll.payment.deposit.service;

import com.ll.payment.deposit.model.entity.Deposit;
import com.ll.payment.deposit.model.entity.DepositHistory;
import com.ll.payment.deposit.model.enums.DepositHistoryType;
import com.ll.payment.deposit.model.exception.DepositNotFoundException;
import com.ll.payment.deposit.model.vo.request.DepositDeleteRequest;
import com.ll.payment.deposit.model.vo.request.DepositTransactionRequest;
import com.ll.payment.deposit.model.vo.response.*;
import com.ll.payment.deposit.repository.DepositRepository;
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
    private final DepositHistoryService depositHistoryService;
    public static final DepositValidationOperation NO_VALIDATION = ref -> {};

    @FunctionalInterface
    public interface DepositOperation {
        DepositHistory apply(Deposit deposit, DepositTransactionRequest request);
    }

    @FunctionalInterface
    public interface DepositValidationOperation {
        void validate(String referenceCode);
    }

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

    @Override
    @Transactional
    public DepositTransactionResponse chargeDeposit(String userCode, DepositTransactionRequest request) {
        return executeDepositTransaction(
                userCode,
                request,
                depositHistoryService::validateDuplicate,
                (deposit, req) -> deposit.charge(req.amount(), req.referenceCode()),
                DepositHistoryType.CHARGE_FAILED
        );
    }

    @Override
    @Transactional
    public void settlementDeposit(String userCode, DepositTransactionRequest request) {
        executeDepositTransaction(
                userCode,
                request,
                NO_VALIDATION,
                (deposit, req) -> deposit.settlement(req.amount(), req.referenceCode()),
                DepositHistoryType.SETTLEMENT_FAILED
        );
    }

    @Override
    @Transactional
    public DepositTransactionResponse withdrawDeposit(String userCode, DepositTransactionRequest request) {
        return executeDepositTransaction(
                userCode,
                request,
                depositHistoryService::validateDuplicate,
                (deposit, req) -> deposit.withdraw(req.amount(), req.referenceCode()),
                DepositHistoryType.WITHDRAW_FAILED
        );
    }

    @Override
    @Transactional
    public DepositTransactionResponse paymentDeposit(String userCode, DepositTransactionRequest request) {
        return executeDepositTransaction(
                userCode,
                request,
                depositHistoryService::validateDuplicate,
                (deposit, req) -> deposit.payment(req.amount(), req.referenceCode()),
                DepositHistoryType.PAYMENT_FAILED
        );
    }

    @Override
    @Transactional
    public DepositTransactionResponse refundDeposit(String userCode, DepositTransactionRequest request) {
        return executeDepositTransaction(
                userCode,
                request,
                depositHistoryService::validateDuplicateForRefund,
                (deposit, req) -> deposit.refund(req.amount(), req.referenceCode()),
                DepositHistoryType.REFUND_FAILED
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DepositHistoryPageResponse getDepositHistoryByUserCode(String userCode, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
        Deposit deposit = findDepositByUserCode(userCode);
        Page<DepositHistory> histories = depositHistoryService.getDepositHistory(deposit, fromDate, toDate, pageable);
        return DepositHistoryPageResponse.from(userCode, histories.map(DepositHistoryResponse::from));
    }

    @Transactional
    public DepositTransactionResponse executeDepositTransaction(
            String userCode,
            DepositTransactionRequest request,
            DepositValidationOperation validationOperation,
            DepositOperation operation,
            DepositHistoryType failType
    ) {
        Deposit deposit = findDepositByUserCode(userCode);
        DepositHistory history;
        try {
            validationOperation.validate(request.referenceCode());
            history = operation.apply(deposit, request);
            depositHistoryService.saveSuccessHistory(history);
            return DepositTransactionResponse.from(deposit.getCode(), history);
        } catch (Exception e) {
            depositHistoryService.saveFailedHistory(deposit, request, failType, e);
            throw e;
        }
    }

    private Deposit findDepositByUserCode(String userCode) {
        return depositRepository.findByUserCode(userCode)
                .orElseThrow(DepositNotFoundException::new);
    }

}