package com.ll.user.controller.swagger;

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
        summary = "01. 회원 목록 조회",
        description = """
                등록된 모든 회원의 정보를 조회합니다.
                """
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "회원 목록 조회 성공",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "200 - 회원 목록 조회 성공",
                                        description = "회원 목록이 정상적으로 조회된 경우의 응답 예시",
                                        value = """
                                                {
                                                  "status": 200,
                                                  "message": "success",
                                                  "data": [
                                                    {
                                                      "userCode": "019b1001-ee08-7c8b-8b32-b29936f5ebfc",
                                                      "name": "홍길동",
                                                      "email": "hong@example.com",
                                                      "phone": "010-1234-5678",
                                                      "role": "USER"
                                                    },
                                                    {
                                                      "userCode": "039c2002-ax09-7c9f-1c11-c03311g8xz21",
                                                      "name": "김철수",
                                                      "email": "chulsoo@example.com",
                                                      "phone": "010-9876-5432",
                                                      "role": "ADMIN"
                                                    }
                                                  ]
                                                }
                                                """
                                )
                        }
                )
        )
})
public @interface UserListApiResponse {
}
