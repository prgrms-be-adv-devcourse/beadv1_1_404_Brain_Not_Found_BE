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
        summary = "01. 토큰 재발급",
        description = """
                저장된 refreshToken 및 deviceCode(쿠키)를 기반으로
                새로운 Access Token 및 Refresh Token을 재발급합니다.
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
                description = "토큰 재발급 성공",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "200 - 토큰 재발급 성공",
                                        description = "refreshToken 검증 후 새로운 accessToken 및 refreshToken 이 발급됩니다.",
                                        value = """
                                                {
                                                  "status": 200,
                                                  "message": "success",
                                                  "data": {
                                                    "accessToken": "eyJhbGciOiJIUzI1NiJ9....",
                                                    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...."
                                                  }
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
        ),
        @ApiResponse(
                responseCode = "404",
                description = "해당 refreshToken 또는 deviceCode 에 대한 유효한 토큰을 찾을 수 없습니다.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "404 - 토큰 검색 실패",
                                        description = "전달된 refreshToken / deviceCode 로 저장된 토큰을 찾을 수 없는 경우",
                                        value = """
                                                {
                                                  "status": 404,
                                                  "message": "리프레시 토큰을 찾을 수 없습니다。",
                                                  "errorCode": "TOKEN_NOT_FOUND"
                                                }
                                                """
                                )
                        }
                )
        )
})
public @interface RefreshTokenApiResponse {
}
