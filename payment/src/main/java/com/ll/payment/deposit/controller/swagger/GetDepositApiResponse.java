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
        summary = "예치금 정보 조회",
        description = "헤더의 userCode 값을 통해 예치금 정보를 조회합니다."
)
@ApiResponses({
        @ApiResponse(
                responseCode ="200",
                description = "예치금 조회 성공",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "200 - 예치금 조회 성공",
                                        description = "정상적으로 예치금 정보를 조회한 경우의 응답 예시입니다.",
                                        value = """
                                                    {
                                                        "status": 200,
                                                        "message": "success",
                                                        "data": {
                                                            "userCode": "019a90ab-fcf3-7413-af08-7121cc99378b",
                                                            "depositCode": "019b0c53-3395-7368-aac8-7bdddab0078e",
                                                            "balance": 100000,
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
public @interface GetDepositApiResponse {
}
