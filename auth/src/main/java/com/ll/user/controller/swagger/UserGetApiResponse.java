package com.ll.user.controller.swagger;

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
        summary = "02. 회원 정보 조회",
        description = """
                현재 로그인된 사용자의 정보를 조회합니다.
                
                - Gateway 에서 AccessToken을 검증 후, UserCode 값을 X-User-Code 헤더에 주입하여 전달합니다.
                - 외부 모듈에서 직접 UserCode 값을 X-User-Code 헤더에 주입하여 전달합니다.
                - 전달받은 X-User-Code 기반으로 회원 정보를 조회합니다.
                """
)
@Parameters({
        @Parameter(
                name = "X-User-Code",
                in = ParameterIn.HEADER,
                description = "로그인된 사용자의 UserCode",
                required = true
        )
})
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "회원 정보 조회 성공",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = @ExampleObject(
                                name = "200 - Success",
                                description = "회원 정보 조회 성공 예시",
                                value = """
                                        {
                                          "status": 200,
                                          "message": "success",
                                          "data": {
                                            "id": 4,
                                            "code": "019b1066-61c7-7191-b26c-13b0fd49e563",
                                            "socialId": "106941175659269314812",
                                            "socialProvider": "GOOGLE",
                                            "email": "als981209@gmail.com",
                                            "name": "김민석",
                                            "role": "USER",
                                            "profileImageUrl": null,
                                            "mannerScore": 5,
                                            "grade": "BRONZE",
                                            "accountStatus": "ACTIVE",
                                            "accountBank": null,
                                            "accountNumber": null,
                                            "createAt": "2025-12-12T11:31:43.815633",
                                            "updatedAt": "2025-12-12T11:31:43.815633"
                                          }
                                        }
                                        """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "필수 헤더 누락",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = @ExampleObject(
                                name = "400 - Missing Header",
                                description = "X-User-Code 헤더가 누락된 경우",
                                value = """
                                        {
                                          "status": 400,
                                          "message": "필수 헤더 'X-User-Code'가 누락되었습니다.",
                                          "errorCode": "BAD_REQUEST"
                                        }
                                        """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "401",
                description = "인증 실패",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = @ExampleObject(
                                name = "401 - Unauthorized",
                                description = "AccessToken 검증 실패 등 인증이 유효하지 않은 경우",
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
                description = "회원 없음",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = @ExampleObject(
                                name = "404 - User Not Found",
                                description = "해당 UserCode로 회원을 찾을 수 없는 경우",
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
public @interface UserGetApiResponse {
}
