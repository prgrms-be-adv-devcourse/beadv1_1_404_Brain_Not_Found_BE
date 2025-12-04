package com.ll.payment.deposit.service;

import com.ll.payment.deposit.model.vo.request.DepositDeleteRequest;
import com.ll.payment.deposit.model.vo.request.DepositTransactionRequest;
import com.ll.payment.deposit.model.vo.response.DepositDeleteResponse;
import com.ll.payment.deposit.model.vo.response.DepositHistoryPageResponse;
import com.ll.payment.deposit.model.vo.response.DepositResponse;
import com.ll.payment.deposit.model.vo.response.DepositTransactionResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface DepositService {

    DepositResponse createDeposit(String userCode);
    DepositResponse getDepositByUserCode(String userCode);
    DepositDeleteResponse deleteDepositByUserCode(String userCode, DepositDeleteRequest request);
    DepositTransactionResponse chargeDeposit(String userCode, DepositTransactionRequest request);
    DepositTransactionResponse withdrawDeposit(String userCode, DepositTransactionRequest request);
    DepositTransactionResponse paymentDeposit(String userCode, DepositTransactionRequest request);
    DepositTransactionResponse refundDeposit(String userCode, DepositTransactionRequest request);
    DepositHistoryPageResponse getDepositHistoryByUserCode(String userCode, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);
}
