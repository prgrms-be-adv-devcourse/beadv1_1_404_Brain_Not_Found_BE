package com.ll.products.domain.product.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import com.ll.products.domain.product.model.entity.ProductStatus;

@Builder
public record ProductUpdateStatusRequest(
        @NotNull(message = "상품 상태는 필수입니다")
        ProductStatus status
) {
}
