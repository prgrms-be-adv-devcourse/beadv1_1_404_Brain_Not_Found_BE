package com.ll.products.domain.product.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import com.ll.products.domain.product.model.dto.ProductImageDto;

import java.util.List;

@Builder
public record ProductCreateRequest(
        @NotBlank(message = "상품명은 필수입니다")
        String name,

        Long categoryId,

        @NotNull(message = "판매자 ID는 필수입니다")
        Long sellerId,

        @NotNull(message = "수량은 필수입니다")
        @Positive(message = "수량은 양수여야 합니다")
        Integer quantity,

        String description,

        @NotNull(message = "가격은 필수입니다")
        @Positive(message = "가격은 양수여야 합니다")
        Integer price,

        @Valid
        List<ProductImageDto> images
) {
}
