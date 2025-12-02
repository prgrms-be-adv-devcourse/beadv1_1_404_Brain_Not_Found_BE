package com.ll.products.global.util;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class S3ImageUrlBuilder {
    public static String buildImageUrl(String fileKey, String s3BaseUrl) {
        if (fileKey == null || fileKey.isBlank()) {
            return null;
        }
        if (s3BaseUrl == null || s3BaseUrl.isBlank()) {
            return fileKey;
        }
        return s3BaseUrl.endsWith("/") ? s3BaseUrl + fileKey : s3BaseUrl + "/" + fileKey;
    }
}
