package com.ll.products.domain.image.model.dto.response;

import lombok.Builder;

@Builder
public record PresignedUrlResponse(
        String presignedUrl,
        String fileKey
) {
}