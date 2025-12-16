package com.ll.products.domain.product.controller.swagger;

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
        summary = "상품 목록 조회",
        description = """
                상품 목록을 페이지네이션으로 조회합니다.

                - 다양한 필터링 옵션을 제공합니다 (판매자, 카테고리, 상태, 상품명).
                - 정렬 기준은 기본적으로 생성일자 내림차순입니다.
                """
)
@Parameters({
        @Parameter(
                name = "sellerCode",
                in = ParameterIn.QUERY,
                description = "판매자 코드로 필터링 (선택)",
                required = false
        ),
        @Parameter(
                name = "categoryId",
                in = ParameterIn.QUERY,
                description = "카테고리 ID로 필터링 (선택)",
                required = false
        ),
        @Parameter(
                name = "status",
                in = ParameterIn.QUERY,
                description = "상품 상태로 필터링 (ACTIVE, INACTIVE, DELETED) (선택)",
                required = false
        ),
        @Parameter(
                name = "name",
                in = ParameterIn.QUERY,
                description = "상품명으로 검색 (선택)",
                required = false
        ),
        @Parameter(
                name = "page",
                in = ParameterIn.QUERY,
                description = "페이지 번호 (0부터 시작)",
                required = false
        ),
        @Parameter(
                name = "size",
                in = ParameterIn.QUERY,
                description = "페이지 크기 (기본값: 10)",
                required = false
        )
})
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "상품 목록 조회 성공",
                content = @Content(
                        mediaType = "application/json",
                        examples = {
                                @ExampleObject(
                                        name = "200 - 상품 목록 조회 성공",
                                        description = "상품 목록이 정상적으로 조회된 경우",
                                        value = """
                                                {
                                                  "status": 200,
                                                  "message": "success",
                                                  "data": {
                                                    "content": [
                                                      {
                                                        "id": 1,
                                                        "code": "PRD001",
                                                        "name": "상품명",
                                                        "price": 10000,
                                                        "inventory": 100,
                                                        "status": "ACTIVE"
                                                      }
                                                    ],
                                                    "totalElements": 1,
                                                    "totalPages": 1,
                                                    "size": 10,
                                                    "number": 0
                                                  }
                                                }
                                                """
                                )
                        }
                )
        )
})
public @interface ProductListApiResponse {
}