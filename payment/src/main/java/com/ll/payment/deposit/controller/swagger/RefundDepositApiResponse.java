package com.ll.payment.deposit.controller.swagger;

import com.ll.core.model.response.BaseResponse;
import com.ll.payment.deposit.model.vo.request.DepositTransactionRequest;
import io.swagger.v3.oas.annotations.Operation;
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
        summary = "예치금 환불",
        description = "X-User-Code 헤더와 요청 바디(예치금 거래 정보)를 이용해 예치금 거래를 수행합니다."
)
@RequestBody(
        description = "예치금 거래 요청 바디",
        required = true,
        content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DepositTransactionRequest.class),
                examples = @ExampleObject(
                        name = "예치금 환불 요청 예시",
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
                description = "예치금 환불 성공",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "200 - 예치금 환불 성공",
                                        description = "정상적으로 예치금에 금액을 환불하는데 성공한 경우의 응답 예시입니다.",
                                        value = """
                                                    {
                                                        "status": 200,
                                                        "message": "success",
                                                        "data": {
                                                            "userCode": "019b0dc8-045a-7967-9e94-5da9553074b8",
                                                            "depositCode": "019b0dc7-7ccc-77d2-aefb-701cd2265953",
                                                            "amount": 100000,
                                                            "balanceBefore": 0,
                                                            "balanceAfter": 100000,
                                                            "historyType": "REFUND",
                                                            "transactionStatus": "COMPLETED",
                                                            "referenceCode": "Refund-019b0c53-3395-7368-aac8-7bdddab01078e",
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
                                        name = "400 - X-User-Code 공란",
                                        description = "필수 헤더 누락 또는 요청 형식이 잘못된 경우의 응답 예시입니다.",
                                        value = """
                                                    {
                                                        "status": 400,
                                                        "message": "userCode 는 공백이거나 null일 수 없습니다.",
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
                description = "환불 요청 처리 중 필요한 데이터를 조회할 수 없을 때의 응답입니다.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "404 - 예치금 계좌 미존재",
                                        description = "환불하려는 사용자의 예치금 계좌가 존재하지 않을 경우의 응답 예시입니다.",
                                        value = """
                                                    {
                                                        "status": 404,
                                                        "message": "해당 입금 계좌를 찾을 수 없습니다.",
                                                        "errorCode": "DEPOSIT_NOT_FOUND"
                                                    }
                                                """
                                ),
                                @ExampleObject(
                                        name = "404 - 환불 대상 거래 미존재",
                                        description = "환불하려는 결제 거래 이력이 존재하지 않을 경우의 응답 예시입니다.",
                                        value = """
                                                    {
                                                        "status": 404,
                                                        "message": "환불 대상 거래 이력을 찾을 수 없습니다.",
                                                        "errorCode": "REFUND_TRANSACTION_NOT_FOUND"
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
                                        description = "ReferenceCode 중복으로 이미 처리된 환불 요청이라고 판단되는 경우의 응답 예시입니다.",
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
                description = "입금 계좌 관련 비즈니스 규칙을 위반한 요청에 대한 오류 응답입니다.",
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
                                ),
                                @ExampleObject(
                                        name = "422 - 환불 금액 불일치",
                                        description = "환불 대상 원거래 금액과 환불 요청 금액이 일치하지 않는 경우의 응답 예시입니다.",
                                        value = """
                                                    {
                                                        "status": 422,
                                                        "message": "환불 금액이 원거래 금액과 일치하지 않습니다.",
                                                        "errorCode": "DEPOSIT_AMOUNT_MISMATCH"
                                                    }
                                                """
                                )
                        }
                )
        )
})
public @interface RefundDepositApiResponse {
}
