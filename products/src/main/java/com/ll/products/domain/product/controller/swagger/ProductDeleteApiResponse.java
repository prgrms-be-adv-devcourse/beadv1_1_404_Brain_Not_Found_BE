package com.ll.products.domain.product.controller.swagger;

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
        summary = "상품 삭제",
        description = """
                상품을 삭제합니다 (Soft Delete).

                - 판매자는 본인의 상품만 삭제 가능합니다.
                - 관리자는 모든 상품을 삭제할 수 있습니다.
                - 실제로 데이터가 삭제되지 않고 상태가 DELETED로 변경됩니다.
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
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "상품 삭제 성공",
                content = @Content(
                        mediaType = "application/json",
                        examples = {
                                @ExampleObject(
                                        name = "200 - 상품 삭제 성공",
                                        description = "상품이 정상적으로 삭제된 경우",
                                        value = """
                                                {
                                                  "status": 200,
                                                  "message": "success",
                                                  "data": null
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
                                                  "message": "상품 삭제 권한이 없습니다.",
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
public @interface ProductDeleteApiResponse {
}