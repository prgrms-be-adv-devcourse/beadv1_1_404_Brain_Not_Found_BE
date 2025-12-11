package com.ll.payment.deposit.controller.swagger;

import com.ll.core.model.response.BaseResponse;
import com.ll.payment.deposit.model.vo.request.DepositTransactionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "예치금 충전",
        description = "X-User-Code 헤더와 요청 바디(예치금 거래 정보)를 이용해 예치금 거래를 수행합니다."
)
@Parameters({
        @Parameter(
                name = "X-User-Code",
                in = ParameterIn.HEADER,
                required = true,
                description = "사용자 고유 코드",
                schema = @Schema(type = "string", example = "019a90ab-fcf3-7413-af08-7121cc99378b")
        )
})
@RequestBody(
        description = "예치금 거래 요청 바디",
        required = true,
        content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DepositTransactionRequest.class),
                examples = @ExampleObject(
                        name = "예치금 충전 요청 예시",
                        value = """
                                {
                                  "amount": 100000,
                                  "referenceCode": "019b0c53-3395-7368-aac8-7bdddab01078e"
                                }
                                """
                )
        )
)
@ApiResponses({
        @ApiResponse(
                responseCode ="200",
                description = "예치금 충전 성공",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "200 - 예치금 충전 성공",
                                        description = "정상적으로 예치금에 금액을 충전하는데 성공한 경우의 응답 예시입니다.",
                                        value = """
                                                    {
                                                        "status": 200,
                                                        "message": "success",
                                                        "data": {
                                                            "userCode": "019b0c7d-ae05-719f-8240-76843c29b5b9",
                                                            "depositCode": "019b0c74-f42b-7a42-aa61-631b814cc01a",
                                                            "amount": 100000,
                                                            "balanceBefore": 0,
                                                            "balanceAfter": 100000,
                                                            "historyType": "CHARGE",
                                                            "transactionStatus": "COMPLETED",
                                                            "referenceCode": "019b0c53-3395-7368-aac8-7bdddab0078e",
                                                            "createdAt": "2025-01-01T00:00:00.000001"
                                                        }
                                                    }
                                                """
                                )
                        }
                )
        ),
        @ApiResponse(
                responseCode ="400" ,
                description = "요청 형식이 올바르지 않습니다.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "400 - X-User-Code 누락",
                                        description = "필수 헤더 누락 또는 요청 형식이 잘못된 경우의 응답 예시입니다.",
                                        value = """
                                                    {
                                                        "status": 400,
                                                        "message": "필수 헤더 'X-User-Code'가 누락되었습니다.",
                                                        "errorCode": "BAD_REQUEST"
                                                    }
                                                """
                                ),
                                @ExampleObject(
                                        name = "400 - amount 누락",
                                        description = "필수 헤더 누락 또는 요청 형식이 잘못된 경우의 응답 예시입니다.",
                                        value = """
                                                    {
                                                        "status": 400,
                                                        "message": "amount 는 필수입력값입니다.",
                                                        "errorCode": "BAD_REQUEST"
                                                    }
                                                """
                                ),
                                @ExampleObject(
                                        name = "400 - amount 0 이하",
                                        description = "필수 헤더 누락 또는 요청 형식이 잘못된 경우의 응답 예시입니다.",
                                        value = """
                                                    {
                                                        "status": 400,
                                                        "message": "금액는 0 보다 커야 합니다.",
                                                        "errorCode": "BAD_REQUEST"
                                                    }
                                                """
                                ),
                                @ExampleObject(
                                        name = "400 - referenceCode 누락",
                                        description = "필수 헤더 누락 또는 요청 형식이 잘못된 경우의 응답 예시입니다.",
                                        value = """
                                                    {
                                                        "status": 400,
                                                        "message": "referenceCode 는 공백이거나 null일 수 없습니다.",
                                                        "errorCode": "BAD_REQUEST"
                                                    }
                                                """
                                )
                        }
                )
        ),
        @ApiResponse(
                responseCode ="404" ,
                description = "해당 입금 계좌를 찾을 수 없습니다.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "404 - 예치금 계좌 미존재",
                                        description = "충전하려는 사용자의 예치금 계좌가 존재하지 않을 경우의 응답 예시입니다.",
                                        value = """
                                                    {
                                                        "status": 404,
                                                        "message": "해당 입금 계좌를 찾을 수 없습니다.",
                                                        "errorCode": "DEPOSIT_NOT_FOUND"
                                                    }
                                                """
                                )
                        }
                )
        ),
        @ApiResponse(
                responseCode ="409" ,
                description = "이미 처리된 거래요청입니다.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "409 - 거래 요청 중복",
                                        description = "ReferenceCode 중복으로 이미 처리된 거래요청이라고 판단되는 경우의 응답 예시입니다.",
                                        value = """
                                                    {
                                                        "status": 409,
                                                        "message": "이미 처리된 거래요청입니다.",
                                                        "errorCode": "TRANSACTION_ALREADY_EXISTS"
                                                    }
                                                """
                                )
                        }
                )
        ),
        @ApiResponse(
                responseCode ="422" ,
                description = "비활성 상태인 입금 계좌를 조작 할 수 없습니다.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "422 - 비활성 계좌 거래",
                                        description = "비활성 상태인 입금 계좌의 조작을 시도했을 경우의 응답 예시입니다.",
                                        value = """
                                                    {
                                                        "status": 422,
                                                        "message": "비활성 상태인 입금 계좌를 조작 할 수 없습니다.",
                                                        "errorCode": "CAN_NOT_TRANSACT_ON_INACTIVE_DEPOSIT"
                                                    }
                                                """
                                )
                        }
                )
        )
})
public @interface ChargeDepositApiResponse {
}
