package com.ll.order.domain.model.vo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ProductRequest(
        @NotBlank(message = "상품 코드는 필수입니다.")
        String productCode,

        @Positive(message = "상품 수량은 1 이상이어야 합니다.")
        int quantity,

        @Positive(message = "상품 가격은 1 이상이어야 합니다.")
        int price,

        String image
) {
}