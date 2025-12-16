package com.ll.products.domain.product.controller.swagger;

import com.ll.core.model.response.BaseResponse;
import com.ll.products.domain.product.model.dto.request.ProductUpdateStatusRequest;
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
        summary = "상품 상태 변경",
        description = """
                상품의 상태를 변경합니다.
                
                - 판매자는 본인의 상품 상태만 변경 가능합니다.
                - 관리자는 모든 상품의 상태를 변경할 수 있습니다.
                - 상태: WAITING, ON_SALE, SOLD_OUT
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
        )
})
@RequestBody(
        required = true,
        content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductUpdateStatusRequest.class),
                examples = {
                        @ExampleObject(
                                name = "상품 상태 변경 요청",
                                value = """
                                        {
                                        "status": "ON_SALE"
                                        }
                                        """
                        )
                }
        )
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "상품 상태 변경 성공",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ProductResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "200 - 상품 상태 변경 성공",
                                        description = "상품 상태가 정상적으로 변경된 경우",
                                        value = """
                                                {
                                                  "status": 200,
                                                  "message": "success",
                                                  "data": {
                                                    "id": 1,
                                                    "code": "019b24c1-6722-7834-aec5-e01db54c6acb",
                                                    "name": "해리포터 안경(모조품)",
                                                    "categoryId": 21,
                                                    "categoryName": "해리포터",
                                                    "sellerCode": "019a90ab-fcf3-7413-af08-7121cc99378b",
                                                    "sellerName": "알 수 없는 판매자",
                                                    "status": "ON_SALE",
                                                    "quantity": 3,
                                                    "description": "미개봉 새상품. 해리포터 마법사의 돌에서 해리가 착용했던 안경의 모조품(중요),",
                                                    "price": 10000,
                                                    "images": [
                                                      {
                                                        "url": "https://team-404-bucket.s3.ap-northeast-2.amazonaws.com/imageFileKeyExample.",
                                                        "sequence": 1,
                                                        "isMain": true
                                                      }
                                                    ],
                                                    "createdAt": "2025-12-16T10:23:33.283724",
                                                    "updatedAt": "2025-12-16T10:24:34.246603"
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
                                        name = "400 - 유효하지 않은 상태값",
                                        description = "유효하지 않은 상태값이 입력된 경우",
                                        value = """
                                                {
                                                  "status": 400,
                                                  "message": "유효하지 않은 상태값입니다.",
                                                  "errorCode": "INVALID_STATUS"
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
                                        description = "본인의 상품이 아닌 경우",
                                        value = """
                                                {
                                                  "status": 403,
                                                  "message": "상품에 대한 권한이 없습니다. 상품코드: 019b24c1-6722-7834-aec5-e01db54c6acb",
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
                                                  "message": "상품을 찾을 수 없습니다. 상품코드: 019b24c1-6722-7834-aec5-e01db54c6acbs",
                                                  "errorCode": "NOT_FOUND"
                                                }
                                                """
                                )
                        }
                )
        )
})
public @interface ProductUpdateStatusApiResponse {
}