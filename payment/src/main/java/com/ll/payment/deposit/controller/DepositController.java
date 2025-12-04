package com.ll.payment.deposit.controller;

import com.ll.core.model.response.BaseResponse;
import com.ll.core.model.vo.common.DateRange;
import com.ll.payment.deposit.model.vo.request.DepositDeleteRequest;
import com.ll.payment.deposit.model.vo.request.DepositTransactionRequest;
import com.ll.payment.deposit.model.vo.response.DepositDeleteResponse;
import com.ll.payment.deposit.model.vo.response.DepositHistoryPageResponse;
import com.ll.payment.deposit.model.vo.response.DepositResponse;
import com.ll.payment.deposit.model.vo.response.DepositTransactionResponse;
import com.ll.payment.deposit.service.DepositService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deposits")
public class DepositController {
    private final DepositService depositService;

    @GetMapping("/ping")
    public ResponseEntity<BaseResponse<String>> pong() {
        System.out.println("DepositController.pong");
        return BaseResponse.ok("Ok");
    }

    @GetMapping
    public ResponseEntity<BaseResponse<DepositResponse>> getDeposit(
            @NotBlank(message = "userCode 는 공백이거나 null일 수 없습니다.")
            @RequestHeader(value = "X-User-Code")
            String userCode

    ) {
        return BaseResponse.ok(depositService.getDepositByUserCode(userCode));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<DepositResponse>> createDeposit(
            @NotBlank(message = "userCode 는 공백이거나 null일 수 없습니다.")
            @RequestHeader(value = "X-User-Code")
            String userCode
    ) {
        return BaseResponse.created(depositService.createDeposit(userCode));
    }

    @PostMapping("/charge")
    public ResponseEntity<BaseResponse<DepositTransactionResponse>> chargeDeposit(
            @NotBlank(message = "userCode 는 공백이거나 null일 수 없습니다.")
            @RequestHeader(value = "X-User-Code")
            String userCode,
            @Valid @RequestBody DepositTransactionRequest request
    ) {
        return BaseResponse.created(depositService.chargeDeposit(userCode, request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<BaseResponse<DepositTransactionResponse>> withdrawDeposit(
            @NotBlank(message = "userCode 는 공백이거나 null일 수 없습니다.")
            @RequestHeader(value = "X-User-Code")
            String userCode,
            @Valid @RequestBody DepositTransactionRequest request
    ) {
        return BaseResponse.created(depositService.withdrawDeposit(userCode, request));
    }

    @PostMapping("/payment")
    public ResponseEntity<BaseResponse<DepositTransactionResponse>> paymentDeposit(
            @NotBlank(message = "userCode 는 공백이거나 null일 수 없습니다.")
            @RequestHeader(value = "X-User-Code")
            String userCode,
            @Valid @RequestBody DepositTransactionRequest request
    ) {
        return BaseResponse.created(depositService.paymentDeposit(userCode, request));
    }

    @PostMapping("/refund")
    public ResponseEntity<BaseResponse<DepositTransactionResponse>> refundDeposit(
            @NotBlank(message = "userCode 는 공백이거나 null일 수 없습니다.")
            @RequestHeader(value = "X-User-Code")
            String userCode,
            @Valid @RequestBody DepositTransactionRequest request
    ) {
        return BaseResponse.created(depositService.refundDeposit(userCode, request));
    }

    @PatchMapping("/close")
    public ResponseEntity<BaseResponse<DepositDeleteResponse>> deleteDeposit(
            @NotBlank(message = "userCode 는 공백이거나 null일 수 없습니다.")
            @RequestHeader(value = "X-User-Code")
            String userCode,
            @Valid @RequestBody DepositDeleteRequest request
    ) {
        return BaseResponse.ok(depositService.deleteDepositByUserCode(userCode, request));
    }

    @GetMapping("/histories")
    public ResponseEntity<BaseResponse<DepositHistoryPageResponse>> getDepositHistories(
            @NotBlank(message = "userCode 는 공백이거나 null일 수 없습니다.")
            @RequestHeader(value = "X-User-Code")
            String userCode,
            @Valid @ModelAttribute DateRange dateRange,
            Pageable pageable
    ) {
        dateRange.validate();
        return BaseResponse.ok(depositService.getDepositHistoryByUserCode(userCode, dateRange.getStartDateTime(), dateRange.getEndDateTime(), pageable));
    }

}
