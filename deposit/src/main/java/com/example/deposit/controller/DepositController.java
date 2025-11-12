package com.example.deposit.controller;

import com.example.core.model.response.BaseResponse;
import com.example.deposit.model.exception.InvalidDateRangeException;
import com.example.deposit.model.vo.request.DepositDeleteRequest;
import com.example.deposit.model.vo.request.DepositTransactionRequest;
import com.example.deposit.model.vo.response.*;
import com.example.deposit.service.DepositService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deposits")
public class DepositController {
    private final DepositService depositService;

    @GetMapping
    public ResponseEntity<BaseResponse<DepositResponse>> getDeposit(
            @NotBlank(message = "userCode 는 공백이거나 null일 수 없습니다.")
            @RequestParam String userCode
    ) {
        return BaseResponse.ok(depositService.getDepositByUserCode(userCode));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<DepositResponse>> createDeposit(
            @NotBlank(message = "userCode 는 공백이거나 null일 수 없습니다.")
            @RequestParam String userCode
    ) {
        return BaseResponse.created(depositService.createDeposit(userCode));
    }

    @PostMapping("/charge")
    public ResponseEntity<BaseResponse<DepositTransactionResponse>> chargeDeposit(
            @NotBlank(message = "userCode 는 공백이거나 null일 수 없습니다.")
            @RequestParam String userCode,
            @Valid @RequestBody DepositTransactionRequest request
    ) {
        return BaseResponse.created(depositService.chargeDeposit(userCode, request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<BaseResponse<DepositTransactionResponse>> withdrawDeposit(
            @NotBlank(message = "userCode 는 공백이거나 null일 수 없습니다.")
            @RequestParam String userCode,
            @Valid @RequestBody DepositTransactionRequest request
    ) {
        return BaseResponse.created(depositService.withdrawDeposit(userCode, request));
    }

    @PatchMapping("/close")
    public ResponseEntity<BaseResponse<DepositDeleteResponse>> deleteDeposit(
            @NotBlank(message = "userCode 는 공백이거나 null일 수 없습니다.")
            @RequestParam String userCode,
            @Valid @RequestBody DepositDeleteRequest request
    ) {
        return BaseResponse.ok(depositService.deleteDepositByUserCode(userCode, request));
    }

    @GetMapping("/histories")
    public ResponseEntity<BaseResponse<DepositHistoryPageResponse>> getDepositHistories(
            @NotBlank(message = "userCode 는 공백이거나 null일 수 없습니다.")
            @RequestParam String userCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Pageable pageable
    ) {
        LocalDateTime start = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime end = toDate != null ? toDate.plusDays(1).atStartOfDay() : null;
        if (fromDate != null && toDate != null && toDate.isBefore(fromDate)) {
            throw new InvalidDateRangeException("조회 종료일은 시작일보다 빠를 수 없습니다.");
        }
        return BaseResponse.ok(depositService.getDepositHistoryByUserCode(userCode, start, end, pageable));
    }

}
