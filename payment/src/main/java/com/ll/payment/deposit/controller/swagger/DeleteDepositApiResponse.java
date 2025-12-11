package com.ll.payment.deposit.controller.swagger;

import com.ll.core.model.response.BaseResponse;
import com.ll.payment.deposit.model.vo.request.DepositDeleteRequest;
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
        summary = "예치금 계좌 삭제",
        description = "X-User-Code 헤더와 요청 바디(삭제 이유)를 통해 특정 예치금 거래 내역을 삭제합니다."
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
        description = "예치금 계좌 삭제 요청 바디",
        required = true,
        content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DepositDeleteRequest.class),
                examples = @ExampleObject(
                        name = "예치금 계좌 삭제 요청 예시",
                        value = """
                                {
                                  "closedReason": "사용자 요청에 의한 계좌 삭제"
                                }
                                """
                )
        )
)
@ApiResponses({
        @ApiResponse(
                responseCode ="200",
                description = "예치금 계좌 삭제 성공",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "200 - 예치금 계좌 삭제 성공",
                                        description = "정상적으로 예치금 계좌를 삭제하는데 성공한 경우의 응답 예시입니다.",
                                        value = """
                                                    {
                                                        "status": 200,
                                                        "message": "success",
                                                        "data": {
                                                            "userCode": "019a90ab-fcf3-7413-af08-7121cc99378b",
                                                            "depositCode": "019b0dd8-a567-762e-9e99-ab8f53b9c4d2",
                                                            "balance": 0,
                                                            "depositStatus": "CLOSED",
                                                            "closedReason": "사용자 요청에 의한 계좌 삭제",
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
                                        name = "400 - closedReason 누락",
                                        description = "필수 헤더 누락 또는 요청 형식이 잘못된 경우의 응답 예시입니다.",
                                        value = """
                                                    {
                                                        "status": 400,
                                                        "message": "closedReason 는 공백이거나 null일 수 없습니다.",
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
                                        description = "삭제하려는 사용자의 예치금 계좌가 존재하지 않을 경우의 응답 예시입니다.",
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
                responseCode ="422" ,
                description = "입금 계좌 관련 비즈니스 규칙을 위반한 요청에 대한 오류 응답입니다.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "422 - 잔액 남아있는 계좌 삭제",
                                        description = "잔액이 남아있는 입금 계좌의 삭제를 시도했을 경우의 응답 예시입니다.",
                                        value = """
                                                    {
                                                        "status": 422,
                                                        "message": "잔액이 남아있는 입금 계좌는 삭제할 수 없습니다.",
                                                        "errorCode": "BALANCE_NOT_EMPTY"
                                                    }
                                                """
                                ),
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
public @interface DeleteDepositApiResponse {
}
