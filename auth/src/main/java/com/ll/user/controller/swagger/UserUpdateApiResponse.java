package com.ll.user.controller.swagger;

import com.ll.core.model.response.BaseResponse;
import com.ll.user.model.vo.request.UserPatchRequest;
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
        summary = "03. 회원 정보 수정",
        description = """
                - UserPatchRequest 기반으로 회원 정보를 수정합니다.
                - 수정된 회원 정보로 AccessToken / RefreshToken 을 재발급합니다.
                - Cookie 의 AccessToken 은 Gateway 가 검증하며 그 과정에서 X-User-Code 가 주입됩니다.
                - Cookie 의 deviceCode 와 userCode 를 조합하여 새로운 토큰을 저장합니다.
                """
)
@Parameters({
        @Parameter(
                name = "X-User-Code",
                in = ParameterIn.HEADER,
                description = "Gateway 에서 AccessToken 검증 후 주입되는 UserCode"
        ),
        @Parameter(
                name = "deviceCode",
                in = ParameterIn.COOKIE,
                description = "기기 식별용 deviceCode (refreshToken 재발급 시 사용)"
        )
})
@RequestBody(
        description = "유저 정보 변경 요청 Body",
        required = true,
        content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserPatchRequest.class),
                examples = @ExampleObject(
                        name = "유저 정보 변경 요청 예시",
                        value = """
                                {
                                  "mannerScore" : 99,
                                  "role" : "SELLER"
                                }
                                """
                )
        )
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "회원 정보 수정 성공",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = @ExampleObject(
                                name = "200 - 회원 정보 수정 성공",
                                description = "정상적으로 회원 정보가 수정된 경우",
                                value = """
                                        {
                                          "status": 200,
                                          "message": "success",
                                          "data": {
                                            "id": 4,
                                            "code": "019b1066-61c7-7191-b26c-13b0fd49e563",
                                            "email": "new_email@example.com",
                                            "name": "김민석",
                                            "role": "USER",
                                            "profileImageUrl": "https://image/new.png",
                                            "mannerScore": 5,
                                            "grade": "BRONZE",
                                            "accountStatus": "ACTIVE",
                                            "createAt": "2025-12-12T11:31:43.815633",
                                            "updatedAt": "2025-12-12T11:59:01.123456"
                                          }
                                        }
                                        """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "요청값 검증 실패",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "400 - Validation 실패",
                                        description = "UserPatchRequest 값이 유효하지 않을 때",
                                        value = """
                                                {
                                                  "status": 400,
                                                  "message": "올바른 형식의 이메일 주소여야 합니다",
                                                  "errorCode": "BAD_REQUEST"
                                                }
                                                """
                                ),
                                @ExampleObject(
                                        name = "400 - X-User-Code 헤더 누락",
                                        description = "X-User-Code 헤더가 누락된 경우",
                                        value = """
                                                {
                                                  "status": 400,
                                                  "message": "필수 헤더 'X-User-Code'가 누락되었습니다.",
                                                  "errorCode": "BAD_REQUEST"
                                                }
                                                """
                                )
                        }
                )
        ),
        @ApiResponse(
                responseCode = "401",
                description = "인증 실패 (AccessToken 또는 UserCode 문제)",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = @ExampleObject(
                                name = "401 - 인증 실패",
                                description = "AccessToken이 만료되었거나 UserCode 가 유효하지 않을 때",
                                value = """
                                        {
                                          "status": 401,
                                          "message": "인증 정보가 유효하지 않습니다.",
                                          "errorCode": "UNAUTHORIZED"
                                        }
                                        """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "대상 회원을 찾을 수 없음",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = @ExampleObject(
                                name = "404 - 회원 없음",
                                description = "수정하려는 회원이 존재하지 않을 때",
                                value = """
                                        {
                                          "status": 404,
                                          "message": "유저를 찾을 수 없습니다.",
                                          "errorCode": "NOT_FOUND"
                                        }
                                        """
                        )
                )
        )
})
public @interface UserUpdateApiResponse {
}
