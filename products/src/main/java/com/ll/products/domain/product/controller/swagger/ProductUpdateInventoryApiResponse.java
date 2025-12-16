package com.ll.products.domain.product.controller.swagger;

import com.ll.core.model.response.BaseResponse;
import com.ll.products.domain.product.model.dto.request.ProductUpdateInventoryRequest;
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
        summary = "상품 재고 변동",
        description = """
                상품의 재고를 증가 또는 감소시킵니다.
                
                - 양수 입력 시 재고 증가, 음수 입력 시 재고 감소
                - 동시성 제어를 위해 비관적 락을 사용합니다.
                """
)
@Parameters({
        @Parameter(
                name = "code",
                in = ParameterIn.PATH,
                description = "상품 코드",
                required = true
        )
})
@RequestBody(
        required = true,
        content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductUpdateInventoryRequest.class),
                examples = {
                        @ExampleObject(
                                name = "상품 재고 변경 요청",
                                value = """
                                        {
                                        "quantity": -1
                                        }
                                        """
                        )
                }
        )
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "재고 변동 성공",
                content = @Content(
                        mediaType = "application/json",
                        examples = {
                                @ExampleObject(
                                        name = "200 - 재고 변동 성공",
                                        description = "재고가 정상적으로 변동된 경우",
                                        value = """
                                                {
                                                  "status": 200,
                                                  "message": "success"
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
                                        name = "400 - 재고 부족",
                                        description = "재고가 부족하여 감소할 수 없는 경우",
                                        value = """
                                                {
                                                  "status": 422,
                                                  "message": "재고가 부족합니다. 상품코드: 019b24c1-6722-7834-aec5-e01db54c6acb, 현재재고: 2",
                                                  "errorCode": "INSUFFICIENT_INVENTORY"
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
                                                  "message": "상품을 찾을 수 없습니다. 상품코드: 019b24c1-6722-7834-aec5-e01db54c6acba",
                                                  "errorCode": "NOT_FOUND"
                                                }
                                                """
                                )
                        }
                )
        )
})
public @interface ProductUpdateInventoryApiResponse {
}