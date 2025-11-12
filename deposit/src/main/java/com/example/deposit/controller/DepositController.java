package com.example.deposit.controller;

import com.example.core.model.response.BaseResponse;
import com.example.deposit.model.vo.request.DepositDeleteRequest;
import com.example.deposit.model.vo.request.DepositTransactionRequest;
import com.example.deposit.model.vo.response.DepositDeleteResponse;
import com.example.deposit.model.vo.response.DepositResponse;
import com.example.deposit.model.vo.response.DepositTransactionResponse;
import com.example.deposit.service.DepositService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deposits")
public class DepositController {
    private final DepositService depositService;

    @GetMapping
    public ResponseEntity<BaseResponse<DepositResponse>> getDeposit(
            @NotNull(message = "userCode 는 필수입력값입니다.")
            @NotBlank(message = "userCode 는 공백일 수 없습니다.")
            @RequestParam String userCode
    ) {
        return BaseResponse.ok(depositService.getDepositByUserCode(userCode));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<DepositResponse>> createDeposit(
            @NotNull(message = "userCode 는 필수입력값입니다.")
            @NotBlank(message = "userCode 는 공백일 수 없습니다.")
            @RequestParam String userCode
    ) {
        return BaseResponse.created(depositService.createDeposit(userCode));
    }

    @PostMapping("/charge")
    public ResponseEntity<BaseResponse<DepositTransactionResponse>> chargeDeposit(
            @NotNull(message = "userCode 는 필수입력값입니다.")
            @NotBlank(message = "userCode 는 공백일 수 없습니다.")
            @RequestParam String userCode,
            @Valid @RequestBody DepositTransactionRequest request
    ) {
        return BaseResponse.created(depositService.chargeDeposit(userCode, request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<BaseResponse<DepositTransactionResponse>> withdrawDeposit(
            @NotNull(message = "userCode 는 필수입력값입니다.")
            @NotBlank(message = "userCode 는 공백일 수 없습니다.")
            @RequestParam String userCode,
            @Valid @RequestBody DepositTransactionRequest request
    ) {
        return BaseResponse.created(depositService.withdrawDeposit(userCode, request));
    }

    @PatchMapping("/close")
    public ResponseEntity<BaseResponse<DepositDeleteResponse>> deleteDeposit(
            @NotNull(message = "userCode 는 필수입력값입니다.")
            @NotBlank(message = "userCode 는 공백일 수 없습니다.")
            @RequestParam String userCode,
            @Valid @RequestBody DepositDeleteRequest request
    ) {
        return BaseResponse.ok(depositService.deleteDepositByUserCode(userCode, request));
    }


}
