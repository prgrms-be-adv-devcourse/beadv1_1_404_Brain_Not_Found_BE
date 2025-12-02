package com.ll.products.domain.product.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import com.ll.products.domain.product.model.entity.ProductImage;

@Builder
public record ProductImageDto(
        @NotBlank(message = "이미지 fileKey는 필수입니다")
        String fileKey,

        @NotNull(message = "이미지 순서는 필수입니다")
        @PositiveOrZero(message = "이미지 순서는 0 이상이어야 합니다")
        Integer sequence,

        @NotNull(message = "메인 이미지 여부는 필수입니다")
        Boolean isMain
) {
    public static ProductImageDto from(ProductImage image) {
        return ProductImageDto.builder()
                .fileKey(image.getFileKey())
                .sequence(image.getSequence())
                .isMain(image.getIsMain())
                .build();
    }

    public ProductImage toEntity() {
        return ProductImage.builder()
                .fileKey(this.fileKey)
                .sequence(this.sequence)
                .isMain(this.isMain)
                .build();
    }

}
