package com.ll.products.domain.product.model.dto.response;

import com.ll.products.domain.product.model.entity.ProductImage;
import lombok.Builder;

@Builder
public record ProductImageResponse(
        String url,
        Integer sequence,
        Boolean isMain
) {
    public static ProductImageResponse from(ProductImage image, String s3BaseUrl) {
        return ProductImageResponse.builder()
                .url(image.getUrl(s3BaseUrl))
                .sequence(image.getSequence())
                .isMain(image.getIsMain())
                .build();
    }
}
