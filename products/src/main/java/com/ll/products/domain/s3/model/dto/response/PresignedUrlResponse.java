package com.ll.products.domain.s3.model.dto.response;

import lombok.Builder;

@Builder
public record PresignedUrlResponse(
        String presignedUrl,
        String fileKey
) {
}