package com.ll.products.domain.product.controller.swagger;

import com.ll.core.model.response.BaseResponse;
import com.ll.products.domain.product.model.dto.request.ProductCreateRequest;
import com.ll.products.domain.product.model.dto.request.ProductUpdateRequest;
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
        summary = "상품 정보 수정",
        description = """
                상품의 정보를 수정합니다.
                
                - 판매자는 본인의 상품만 수정 가능합니다.
                - 관리자는 모든 상품을 수정할 수 있습니다.
                - 상품명, 설명, 가격 등의 정보를 수정할 수 있습니다.
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
                description = "사용자 코드",
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
                schema = @Schema(implementation = ProductUpdateRequest.class),
                examples = {
                        @ExampleObject(
                                name = "상품 수정 요청",
                                value = """
                                        {
                                        "name": "해리포터 안경(모조품)",
                                        "categoryId": 21,
                                        "description": "미개봉 새상품. 해리포터 마법사의 돌에서 해리가 착용했던 안경의 모조품(중요),",
                                        "price": 10000,
                                        "quantity": 3,
                                        "addImages": [
                                        {
                                        "fileKey": "imageFileKeyExample.",
                                        "sequence": 1,
                                        "isMain" : true
                                        }
                                        ],
                                        "deleteImageKeys":[
                                        "fileKeyExample"
                                        ]
                                        }
                                        """
                        )
                }
        )
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "상품 수정 성공",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "200 - 상품 수정 성공",
                                        description = "상품 정보가 정상적으로 수정된 경우",
                                        value = """
                                                {
                                                  "status": 200,
                                                  "message": "success",
                                                  "data": {
                                                    "id": 1,
                                                    "code": "PRD001",
                                                    "name": "수정된 상품명",
                                                    "description": "수정된 상품 설명",
                                                    "price": 15000,
                                                    "quantity": 100,
                                                    "status": "ACTIVE"
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
                                                  "message": "필수 필드가 누락되었습니다.",
                                                  "errorCode": "INVALID_INPUT"
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
                                        description = "본인의 상품이 아니거나 권한이 없는 경우",
                                        value = """
                                                {
                                                  "status": 403,
                                                  "message": "상품 수정 권한이 없습니다.",
                                                  "errorCode": "FORBIDDEN"
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
                                                  "message": "상품을 찾을 수 없습니다.",
                                                  "errorCode": "PRODUCT_NOT_FOUND"
                                                }
                                                """
                                )
                        }
                )
        )
})
public @interface ProductUpdateApiResponse {
}