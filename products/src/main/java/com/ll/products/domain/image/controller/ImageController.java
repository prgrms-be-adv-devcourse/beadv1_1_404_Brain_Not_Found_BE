package com.ll.products.domain.image.controller;

import com.ll.core.model.response.BaseResponse;
import com.ll.products.domain.image.model.dto.response.PresignedUrlResponse;
import com.ll.products.domain.image.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final S3Service s3Service;

    // 1. presigned url 생성
    @GetMapping("/presigned-url")
    public ResponseEntity<BaseResponse<PresignedUrlResponse>> getPresignedUploadUrl(
            @RequestParam("filename") String filename
    ) {
        return BaseResponse.ok(s3Service.generatePresignedUploadUrl(filename));
    }

    // 2. 이미지 파일 삭제
    @DeleteMapping
    public ResponseEntity<BaseResponse<Void>> deleteImage(
            @RequestParam("fileKey") String fileKey
    ) {
        s3Service.deleteImage(fileKey);
        return BaseResponse.ok(null);
    }
}