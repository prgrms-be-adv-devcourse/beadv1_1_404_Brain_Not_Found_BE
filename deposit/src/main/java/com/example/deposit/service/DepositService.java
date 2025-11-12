package com.example.deposit.service;

import com.example.deposit.model.vo.request.DepositDeleteRequest;
import com.example.deposit.model.vo.request.DepositTransactionRequest;
import com.example.deposit.model.vo.response.DepositDeleteResponse;
import com.example.deposit.model.vo.response.DepositHistoryResponse;
import com.example.deposit.model.vo.response.DepositTransactionResponse;
import com.example.deposit.model.vo.response.DepositResponse;

import java.util.List;

public interface DepositService {

    DepositResponse createDeposit(String userCode);
    DepositResponse getDepositByUserCode(String userCode);
    DepositDeleteResponse deleteDepositByUserCode(String userCode, DepositDeleteRequest request);
    DepositTransactionResponse chargeDeposit(String userCode, DepositTransactionRequest request);
    DepositTransactionResponse withdrawDeposit(String userCode, DepositTransactionRequest request);

}
