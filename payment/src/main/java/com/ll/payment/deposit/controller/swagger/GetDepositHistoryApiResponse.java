package com.ll.payment.deposit.controller.swagger;

import com.ll.core.model.response.BaseResponse;
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
        summary = "예치금 거래 내역 조회",
        description = "Header의 X-User-Code, 기간 필터(DateRange), 페이지네이션(pageable)을 이용해 거래 내역을 조회합니다."
)
@Parameters({
        @Parameter(
                name = "X-User-Code",
                in = ParameterIn.HEADER,
                description = "사용자 고유 코드 (필수)",
                required = true,
                schema = @Schema(type = "string", example = "019a90ab-fcf3-7413-af08-7121cc99378b")
        ),
        @Parameter(
                name = "fromDate",
                in = ParameterIn.QUERY,
                description = "조회 시작일 (yyyy-MM-dd)",
                schema = @Schema(type = "string", format = "date", example = "2025-01-01")
        ),
        @Parameter(
                name = "toDate",
                in = ParameterIn.QUERY,
                description = "조회 종료일 (yyyy-MM-dd)",
                schema = @Schema(type = "string", format = "date", example = "2025-12-31")
        ),
        @Parameter(
                name = "page",
                in = ParameterIn.QUERY,
                description = "페이지 번호 (0부터 시작)",
                schema = @Schema(type = "integer", defaultValue = "0")
        ),
        @Parameter(
                name = "size",
                in = ParameterIn.QUERY,
                description = "페이지 크기",
                schema = @Schema(type = "integer", defaultValue = "20")
        ),
        @Parameter(
                name = "sort",
                in = ParameterIn.QUERY,
                description = "정렬 기준 (예: createdAt,desc)",
                schema = @Schema(type = "string")
        )
})
@ApiResponses({
        @ApiResponse(
                responseCode ="200",
                description = "예치금 계좌 거래 내역 조회 성공",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "200 - 예치금 계좌 거래 내역 조회 성공",
                                        description = "정상적으로 예치금 계좌 거래 내역을 조회하는데 성공한 경우의 응답 예시입니다.",
                                        value = """
                                                    {
                                                        "status": 200,
                                                        "message": "success",
                                                        "data": {
                                                            "userCode": "019a90ab-fcf3-7413-af08-7121cc99378b",
                                                            "page": 0,
                                                            "size": 20,
                                                            "totalElements": 3,
                                                            "totalPages": 1,
                                                            "hasNext": false,
                                                            "hasPrevious": false,
                                                            "content": [
                                                                {
                                                                    "depositHistoryCode": "019b0e22-d9ad-7a49-b4cc-f2cfed75b8f9",
                                                                    "amount": 100000,
                                                                    "balanceBefore": 0,
                                                                    "balanceAfter": 100000,
                                                                    "historyType": "REFUND",
                                                                    "transactionStatus": "COMPLETED",
                                                                    "referenceCode": "Refund-019b0c53-3395-7368-aac8-7bdddab01078e",
                                                                    "createdAt": "2025-12-12T00:58:43.629759",
                                                                    "updatedAt": "2025-12-12T00:58:43.629759"
                                                                },
                                                                {
                                                                    "depositHistoryCode": "019b0e22-bac5-766e-9651-0332ae295fec",
                                                                    "amount": 100000,
                                                                    "balanceBefore": 0,
                                                                    "balanceAfter": 0,
                                                                    "historyType": "REFUND_FAILED",
                                                                    "transactionStatus": "FAILED",
                                                                    "referenceCode": "019b0c53-3395-7368-aac8-7bdddab01078e_FAILED_1765468715716_비활성 상태인 입금 계좌를 조작 할 수 없습니다.",
                                                                    "createdAt": "2025-12-12T00:58:35.717781",
                                                                    "updatedAt": "2025-12-12T00:58:35.717781"
                                                                },
                                                                {
                                                                    "depositHistoryCode": "019b0e20-61d7-746d-beb7-e224aa07bb7e",
                                                                    "amount": 100000,
                                                                    "balanceBefore": 100000,
                                                                    "balanceAfter": 0,
                                                                    "historyType": "PAYMENT",
                                                                    "transactionStatus": "COMPLETED",
                                                                    "referenceCode": "019b0c53-3395-7368-aac8-7bdddab01078e",
                                                                    "createdAt": "2025-12-12T00:56:01.88318",
                                                                    "updatedAt": "2025-12-12T00:56:01.88318"
                                                                }
                                                            ]
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
                                        description = "조회하려는 사용자의 예치금 계좌가 존재하지 않을 경우의 응답 예시입니다.",
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
        )
})
public @interface GetDepositHistoryApiResponse {
}
