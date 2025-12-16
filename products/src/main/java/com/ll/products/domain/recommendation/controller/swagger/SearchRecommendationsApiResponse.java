package com.ll.products.domain.recommendation.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "검색어 기반 상품 추천",
        description = """
                검색 키워드를 기반으로 관련 상품을 추천합니다.
                
                - 키워드와 관련도가 높은 상품들을 찾습니다.
                - Elasticsearch를 활용한 전문 검색을 수행합니다.
                """
)
@Parameters({
        @Parameter(
                name = "keyword",
                in = ParameterIn.QUERY,
                description = "검색 키워드",
                required = true
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
                description = "검색어 기반 추천 성공",
                content = @Content(
                        mediaType = "application/json",
                        examples = {
                                @ExampleObject(
                                        name = "200 - 검색어 기반 추천 성공",
                                        description = "검색 결과가 정상적으로 조회된 경우",
                                        value = """
                                                [
                                                  {
                                                    "productCode": "019b24c1-6722-7834-aec5-e01db54c6acb",
                                                    "name": "해리포터 안경(모조품)",
                                                    "description": "미개봉 새상품. 해리포터 마법사의 돌에서 해리가 착용했던 안경의 모조품(중요),",
                                                    "categoryName": "해리포터",
                                                    "price": 10000,
                                                    "status": "ON_SALE",
                                                    "score": 0.27883434
                                                  }
                                                ]
                                                """
                                )
                        }
                )
        )
})
public @interface SearchRecommendationsApiResponse {
}