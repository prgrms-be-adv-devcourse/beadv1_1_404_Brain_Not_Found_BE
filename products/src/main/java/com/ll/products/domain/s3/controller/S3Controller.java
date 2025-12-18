package com.ll.products.domain.s3.controller;

import com.ll.core.model.response.BaseResponse;
import com.ll.products.domain.s3.model.dto.response.PresignedUrlResponse;
import com.ll.products.domain.s3.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Image", description = "S3 관련 API")
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    @Operation(summary = "presigned url 생성")
    @GetMapping("/presigned-url")
    public ResponseEntity<BaseResponse<PresignedUrlResponse>> getPresignedUploadUrl(
            @RequestParam("filename") String filename
    ) {
        return BaseResponse.ok(s3Service.generatePresignedUploadUrl(filename));
    }
}