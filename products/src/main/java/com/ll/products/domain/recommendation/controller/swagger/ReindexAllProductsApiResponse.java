package com.ll.products.domain.recommendation.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "전체 상품 재색인",
        description = """
                Elasticsearch의 모든 상품 데이터를 재색인합니다 (관리자용).

                - 데이터베이스의 모든 상품 정보를 Elasticsearch에 다시 저장합니다.
                - 검색 성능 개선 또는 데이터 동기화가 필요할 때 사용합니다.
                """
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "재색인 시작 성공",
                content = @Content(
                        mediaType = "application/json",
                        examples = {
                                @ExampleObject(
                                        name = "200 - 재색인 시작 성공",
                                        description = "재색인 작업이 정상적으로 시작된 경우",
                                        value = """
                                                "전체 상품 재색인이 시작되었습니다."
                                                """
                                )
                        }
                )
        )
})
public @interface ReindexAllProductsApiResponse {
}