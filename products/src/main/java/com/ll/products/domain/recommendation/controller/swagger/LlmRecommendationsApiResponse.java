package com.ll.products.domain.recommendation.controller.swagger;

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
        summary = "LLM 기반 상품 추천",
        description = """
                LLM(Large Language Model)을 활용하여 사용자의 조회 기록을 분석하고 상품을 추천합니다.

                - 사용자의 조회 기록을 LLM이 분석하여 맥락을 파악합니다.
                - 더 정교하고 맥락을 고려한 추천을 제공합니다.
                """
)
@Parameters({
        @Parameter(
                name = "X-User-Code",
                in = ParameterIn.HEADER,
                description = "사용자 코드",
                required = true,
                example = "019a90ab-fcf3-7413-af08-7121cc99378b"
        ),
        @Parameter(
                name = "limit",
                in = ParameterIn.QUERY,
                description = "추천 결과 개수 (기본값: 10)",
                required = false
        )
})
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "LLM 기반 추천 성공",
                content = @Content(
                        mediaType = "application/json",
                        examples = {
                                @ExampleObject(
                                        name = "200 - LLM 기반 추천 성공",
                                        description = "추천 결과가 정상적으로 조회된 경우",
                                        value = """
                                                [
                                                  {
                                                    "productCode": "PRD001",
                                                    "productName": "추천 상품 1",
                                                    "price": 10000,
                                                    "score": 0.96
                                                  },
                                                  {
                                                    "productCode": "PRD002",
                                                    "productName": "추천 상품 2",
                                                    "price": 12000,
                                                    "score": 0.93
                                                  }
                                                ]
                                                """
                                )
                        }
                )
        )
})
public @interface LlmRecommendationsApiResponse {
}