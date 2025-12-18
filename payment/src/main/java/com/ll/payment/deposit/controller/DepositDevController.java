package com.ll.payment.deposit.controller;

import com.ll.core.model.response.BaseResponse;
import com.ll.core.model.vo.common.DateRange;
import com.ll.payment.deposit.controller.swagger.*;
import com.ll.payment.deposit.model.vo.request.DepositDeleteRequest;
import com.ll.payment.deposit.model.vo.request.DepositTransactionRequest;
import com.ll.payment.deposit.model.vo.response.DepositDeleteResponse;
import com.ll.payment.deposit.model.vo.response.DepositHistoryPageResponse;
import com.ll.payment.deposit.model.vo.response.DepositResponse;
import com.ll.payment.deposit.model.vo.response.DepositTransactionResponse;
import com.ll.payment.deposit.service.DepositService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Deposit", description = "예치금 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deposits")
@Profile("!prod")
public class DepositDevController {
    private final DepositService depositService;

    @GetMapping
    @GetDepositApiResponse
    public ResponseEntity<BaseResponse<DepositResponse>> getDeposit(
            @NotBlank(message = "userCode 는 공백이거나 null일 수 없습니다.")
            @RequestHeader(value = "X-User-Code")
            String userCode

    ) {
        return BaseResponse.ok(depositService.getDepositByUserCode(userCode));
    }

    @PostMapping
    @CreateDepositApiResponse
    public ResponseEntity<BaseResponse<DepositResponse>> createDeposit(
            @NotBlank(message = "userCode 는 공백이거나 null일 수 없습니다.")
            @RequestHeader(value = "X-User-Code")
            String userCode
    ) {
        return BaseResponse.created(depositService.createDeposit(userCode));
    }

    @PostMapping("/charge")
    @ChargeDepositApiResponse
    public ResponseEntity<BaseResponse<DepositTransactionResponse>> chargeDeposit(
            @NotBlank(message = "userCode 는 공백이거나 null일 수 없습니다.")
            @RequestHeader(value = "X-User-Code")
            String userCode,
            @Parameter(hidden = true)
            @Valid @RequestBody DepositTransactionRequest request
    ) {
        return BaseResponse.ok(depositService.chargeDeposit(userCode, request));
    }

    @PostMapping("/withdraw")
    @WithdrawDepositApiResponse
    public ResponseEntity<BaseResponse<DepositTransactionResponse>> withdrawDeposit(
            @NotBlank(message = "userCode 는 공백이거나 null일 수 없습니다.")
            @RequestHeader(value = "X-User-Code")
            String userCode,
            @Parameter(hidden = true)
            @Valid @RequestBody DepositTransactionRequest request
    ) {
        return BaseResponse.ok(depositService.withdrawDeposit(userCode, request));
    }

    @PostMapping("/payment")
    @PaymentDepositApiResponse
    public ResponseEntity<BaseResponse<DepositTransactionResponse>> paymentDeposit(
            @NotBlank(message = "userCode 는 공백이거나 null일 수 없습니다.")
            @RequestHeader(value = "X-User-Code")
            String userCode,
            @Parameter(hidden = true)
            @Valid @RequestBody DepositTransactionRequest request
    ) {
        return BaseResponse.ok(depositService.paymentDeposit(userCode, request));
    }

    @PostMapping("/refund")
    @RefundDepositApiResponse
    public ResponseEntity<BaseResponse<DepositTransactionResponse>> refundDeposit(
            @NotBlank(message = "userCode 는 공백이거나 null일 수 없습니다.")
            @RequestHeader(value = "X-User-Code")
            String userCode,
            @Parameter(hidden = true)
            @Valid @RequestBody DepositTransactionRequest request
    ) {
        return BaseResponse.ok(depositService.refundDeposit(userCode, request));
    }

    @PatchMapping("/close")
    @DeleteDepositApiResponse
    public ResponseEntity<BaseResponse<DepositDeleteResponse>> deleteDeposit(
            @NotBlank(message = "userCode 는 공백이거나 null일 수 없습니다.")
            @RequestHeader(value = "X-User-Code")
            String userCode,
            @Parameter(hidden = true)
            @Valid @RequestBody DepositDeleteRequest request
    ) {
        return BaseResponse.ok(depositService.deleteDepositByUserCode(userCode, request));
    }

    @GetMapping("/histories")
    @GetDepositHistoryApiResponse
    public ResponseEntity<BaseResponse<DepositHistoryPageResponse>> getDepositHistories(
            @NotBlank(message = "userCode 는 공백이거나 null일 수 없습니다.")
            @RequestHeader(value = "X-User-Code")
            String userCode,
            @Parameter(hidden = true)
            @Valid @ModelAttribute DateRange dateRange,
            @Parameter(hidden = true)
            Pageable pageable
    ) {
        dateRange.validate();
        return BaseResponse.ok(depositService.getDepositHistoryByUserCode(userCode, dateRange.getStartDateTime(), dateRange.getEndDateTime(), pageable));
    }

}
