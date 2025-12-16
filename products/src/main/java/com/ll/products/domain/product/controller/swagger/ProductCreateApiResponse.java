package com.ll.products.domain.product.controller.swagger;

import com.ll.core.model.response.BaseResponse;
import com.ll.products.domain.product.model.dto.request.ProductCreateRequest;
import com.ll.products.domain.product.model.dto.response.ProductResponse;
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
        summary = "상품 생성",
        description = """
                새로운 상품을 등록합니다.
                
                - 판매자 권한이 필요합니다.
                - 상품명, 설명, 가격, 재고 등의 정보를 입력받습니다.
                """
)
@Parameters({
        @Parameter(
                name = "X-User-Code",
                in = ParameterIn.HEADER,
                description = "판매자 코드",
                required = true,
                example = "019a90ab-fcf3-7413-af08-7121cc99378b"
        ),
        @Parameter(
                name = "X-Role",
                in = ParameterIn.HEADER,
                description = "사용자 권한",
                required = true,
                example = "SELLER"
        )
})
@RequestBody(
        required = true,
        content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductCreateRequest.class),
                examples = {
                        @ExampleObject(
                                name = "상품 생성 요청",
                                value = """
                                        {
                                        "name": "해리포터 안경",
                                        "categoryId": 21,
                                        "description": "미개봉 새상품. 해리포터 마법사의 돌에서 해리가 착용했던 안경의 모조품,",
                                        "price": 22000,
                                        "quantity": 3,
                                        "images": [
                                        {
                                        "fileKey": "fileKeyExample",
                                        "sequence": 0,
                                        "isMain" : true
                                        }
                                        ]
                                        }
                                        """
                        )
                }
        )
)
@ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "상품 생성 성공",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "201 - 상품 생성 성공",
                                        description = "상품이 정상적으로 생성된 경우",
                                        value = """
                                                  {
                                                  "status": 201,
                                                  "message": "success",
                                                  "data": {
                                                    "id": 1,
                                                    "code": "019b24ab-8593-7cfe-bf2d-535bd8a327fa",
                                                    "name": "해리포터 안경",
                                                    "categoryId": 21,
                                                    "categoryName": "해리포터",
                                                    "sellerCode": "019a90ab-fcf3-7413-af08-7121cc99378b",
                                                    "sellerName": "판매자01",
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
                responseCode = "400",
                description = "잘못된 요청",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "400 - 필수 필드 누락",
                                        description = "필수 입력값이 누락된 경우",
                                        value = """
                                                {
                                                  "status": 400,
                                                  "message": "상품명은 필수입니다",
                                                  "errorCode": "BAD_REQUEST"
                                                }
                                                """
                                )
                        }
                )
        ),
        @ApiResponse(
                responseCode = "403",
                description = "권한 없음",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "403 - 권한 없음",
                                        description = "판매자 권한이 없는 경우",
                                        value = """
                                                {
                                                  "status": 403,
                                                  "message": "상품에 대한 권한이 없습니다. 상품코드: 상품 생성 권한 없음",
                                                  "errorCode": "FORBIDDEN"
                                                }
                                                """
                                )
                        }
                )
        )
})
public @interface ProductCreateApiResponse {
}