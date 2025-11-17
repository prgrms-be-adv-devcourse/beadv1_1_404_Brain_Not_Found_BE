package com.ll.deposit.service;

import com.ll.deposit.model.vo.request.DepositDeleteRequest;
import com.ll.deposit.model.vo.request.DepositTransactionRequest;
import com.ll.deposit.model.vo.response.*;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface DepositService {

    DepositResponse createDeposit(String userCode);
    DepositResponse getDepositByUserCode(String userCode);
    DepositDeleteResponse deleteDepositByUserCode(String userCode, DepositDeleteRequest request);
    DepositTransactionResponse chargeDeposit(String userCode, DepositTransactionRequest request);
    DepositTransactionResponse withdrawDeposit(String userCode, DepositTransactionRequest request);
    DepositHistoryPageResponse getDepositHistoryByUserCode(String userCode, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);
}
