package com.ll.products.domain.product.model.dto.request;

import jakarta.validation.constraints.NotNull;

public record ProductUpdateInventoryRequest(
        @NotNull(message = "수량은 필수입니다")
        Integer quantity
) {
}