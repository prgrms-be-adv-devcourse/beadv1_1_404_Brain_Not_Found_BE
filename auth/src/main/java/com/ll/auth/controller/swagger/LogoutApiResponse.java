package com.ll.auth.controller.swagger;

import com.ll.core.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
        summary = "02. 로그아웃",
        description = """
                쿠키(refreshToken, deviceCode)에 저장된 정보를 기반으로
                사용자 인증 정보를 삭제합니다.
                
                - Redis 및 DB에서 해당 refreshToken 정보를 제거하고
                - 브라우저 쿠키의 refreshToken, accessToken, deviceCode 를 만료시킵니다.
                """
)
@Parameters({
        @Parameter(
                name = "refreshToken",
                in = ParameterIn.COOKIE,
                description = "사용자의 Refresh Token 값"
        ),
        @Parameter(
                name = "deviceCode",
                in = ParameterIn.COOKIE,
                description = "기기를 식별하기 위한 고유 deviceCode 값"
        )
})
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "로그아웃 성공",
                content = @Content(
                        mediaType = "application/json",
                        examples = {
                                @ExampleObject(
                                        name = "200 - 로그아웃 성공",
                                        description = "토큰 삭제 및 쿠키 만료가 정상적으로 처리된 경우",
                                        value = """
                                                {
                                                  "status": 200,
                                                  "message": "success",
                                                  "data": null
                                                }
                                                """
                                )
                        }
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "필수 쿠키(refreshToken 또는 deviceCode)가 누락되었습니다.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "400 - refreshToken 누락",
                                        description = "refreshToken 쿠키가 전달되지 않은 경우",
                                        value = """
                                                {
                                                  "status": 400,
                                                  "message": "요청에 refreshToken 이 제공되지 않았습니다.",
                                                  "errorCode": "TOKEN_NOT_PROVIDED"
                                                }
                                                """
                                ),
                                @ExampleObject(
                                        name = "400 - deviceCode 누락",
                                        description = "deviceCode 쿠키가 전달되지 않은 경우",
                                        value = """
                                                {
                                                  "status": 400,
                                                  "message": "요청에 deviceCode 가 제공되지 않았습니다.",
                                                  "errorCode": "DEVICE_CODE_NOT_PROVIDED"
                                                }
                                                """
                                )
                        }
                )
        )
})
public @interface LogoutApiResponse {
}
