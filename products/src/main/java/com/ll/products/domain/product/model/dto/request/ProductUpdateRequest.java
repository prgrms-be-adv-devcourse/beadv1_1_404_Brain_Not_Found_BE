package com.ll.products.domain.product.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import com.ll.products.domain.product.model.dto.ProductImageDto;

import java.util.List;

@Builder
public record ProductUpdateRequest(
        String name,

        Long categoryId,

        @Positive(message = "수량은 양수여야 합니다")
        Integer quantity,

        String description,

        @Positive(message = "가격은 양수여야 합니다")
        Integer price,

        @Valid
        @Size(max = 5)
        List<ProductImageDto> addImages,

        List<String> deleteImageKeys

) {
}
