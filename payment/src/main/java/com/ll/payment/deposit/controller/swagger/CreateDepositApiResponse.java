package com.ll.payment.deposit.controller.swagger;

import com.ll.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "예치금 계좌 생성",
        description = "헤더의 userCode 값을 통해 예치금 계좌를 생성합니다."
)
@ApiResponses({
        @ApiResponse(
                responseCode ="201",
                description = "예치금 계좌 생성",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "201 - 예치금 계좌 생성",
                                        description = "정상적으로 예치금 계좌를 생성하는데 성공한 경우의 응답 예시입니다.",
                                        value = """
                                                    {
                                                        "status": 201,
                                                        "message": "success",
                                                        "data": {
                                                            "userCode": "019a90ab-fcf3-7413-af08-7121cc99378b",
                                                            "depositCode": "019b0c69-143e-78fc-8cf4-64454ef06442",
                                                            "balance": 0,
                                                            "depositStatus": "ACTIVE",
                                                            "createdAt": "2025-01-01T00:00:00.000001",
                                                            "updatedAt": "2025-01-01T00:00:00.000001"
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
                responseCode ="409" ,
                description = "이미 존재하는 입금 계좌입니다.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "409 - 이미 존재하는 입금 계좌",
                                        description = "해당 사용자에게 이미 예치금 계좌가 존재하는 경우 발생하는 충돌 오류 예시입니다.",
                                        value = """
                                                    {
                                                        "status": 409,
                                                        "message": "이미 존재하는 입금 계좌입니다.",
                                                        "errorCode": "DEPOSIT_ALREADY_EXISTS"
                                                    }
                                                """
                                )
                        }
                )
        )
})
public @interface CreateDepositApiResponse {
}
