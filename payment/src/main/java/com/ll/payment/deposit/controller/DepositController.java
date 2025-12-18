package com.ll.payment.deposit.controller;

import com.ll.core.model.response.BaseResponse;
import com.ll.core.model.vo.common.DateRange;
import com.ll.payment.deposit.controller.swagger.*;
import com.ll.payment.deposit.model.vo.request.DepositDeleteRequest;
import com.ll.payment.deposit.model.vo.response.DepositDeleteResponse;
import com.ll.payment.deposit.model.vo.response.DepositHistoryPageResponse;
import com.ll.payment.deposit.model.vo.response.DepositResponse;
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
@Profile("prod")
public class DepositController {
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
