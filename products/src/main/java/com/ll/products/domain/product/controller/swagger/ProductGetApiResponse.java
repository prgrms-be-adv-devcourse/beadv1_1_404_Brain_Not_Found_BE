package com.ll.products.domain.product.controller.swagger;

import com.ll.core.model.response.BaseResponse;
import com.ll.products.domain.product.model.dto.response.ProductResponse;
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
        summary = "상품 상세 조회",
        description = """
                상품 코드를 통해 상품의 상세 정보를 조회합니다.
                
                - 로그인한 사용자의 경우 조회 이력이 저장됩니다.
                - 비로그인 사용자도 조회 가능합니다.
                """
)
@Parameters({
        @Parameter(
                name = "code",
                in = ParameterIn.PATH,
                description = "상품 코드",
                required = true
        ),
        @Parameter(
                name = "X-User-Code",
                in = ParameterIn.HEADER,
                description = "사용자 코드 (선택)",
                required = false
        )
})
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "상품 조회 성공",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "200 - 상품 조회 성공",
                                        description = "상품 정보가 정상적으로 조회된 경우",
                                        value = """
                                                {
                                                  "status": 200,
                                                  "message": "success",
                                                  "data": {
                                                    "id": 1,
                                                    "code": "019b24ab-8593-7cfe-bf2d-535bd8a327fa",
                                                    "name": "해리포터 안경",
                                                    "categoryId": 21,
                                                    "categoryName": "해리포터",
                                                    "sellerCode": "019a90ab-fcf3-7413-af08-7121cc99378b",
                                                    "sellerName": "알 수 없는 판매자",
                                                    "status": "WAITING",
                                                    "quantity": 3,
                                                    "description": "미개봉 새상품. 해리포터 마법사의 돌에서 해리가 착용했던 안경의 모조품,",
                                                    "price": 22000,
                                                    "images": [
                                                      {
                                                        "url": "https://team-404-bucket.s3.ap-northeast-2.amazonaws.com/fileKeyExample",
                                                        "sequence": 0,
                                                        "isMain": true
                                                      }
                                                    ],
                                                    "createdAt": "2025-12-16T09:59:39.286104",
                                                    "updatedAt": "2025-12-16T09:59:39.286104"
                                                  }
                                                }
                                                """
                                )
                        }
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "상품을 찾을 수 없음",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "404 - 상품 없음",
                                        description = "해당 코드의 상품이 존재하지 않는 경우",
                                        value = """
                                                {
                                                  "status": 404,
                                                  "message": "상품을 찾을 수 없습니다. 상품코드: 019b24ab-8593-7cfe-bf2d-535bd8a327fa1",
                                                  "errorCode": "NOT_FOUND"
                                                }
                                                """
                                )
                        }
                )
        )
})
public @interface ProductGetApiResponse {
}