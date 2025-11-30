package com.ll.products.domain.image.service;

import com.ll.products.domain.image.exception.InvalidFileExtensionException;
import com.ll.products.domain.image.exception.InvalidFileNameException;
import com.ll.products.domain.image.exception.S3DeleteException;
import com.ll.products.domain.image.exception.S3UploadException;
import com.ll.products.domain.image.model.dto.response.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private static final String IMAGE_PATH = "images/";
    private static final int PRESIGNED_URL_MINUTES = 20;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    // 1. presigned url 생성
    public PresignedUrlResponse generatePresignedUploadUrl(String filename) {
        String extension = getFileExtension(filename);
        validateExtension(extension);
        String fileKey = IMAGE_PATH + generateFileName(extension);
        String presignedUrl = generatePresignedPutUrl(fileKey, extension);

        return PresignedUrlResponse.builder()
                .presignedUrl(presignedUrl)
                .fileKey(fileKey)
                .build();
    }

    // 2.이미지 삭제
    public void deleteImage(String fileKey) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            log.info("이미지 삭제 성공: {}", fileKey);
        } catch (Exception e) {
            log.error("이미지 삭제 실패: {}", e.getMessage());
            throw new S3DeleteException(fileKey);
        }
    }


    private String generatePresignedPutUrl(String fileKey, String extension) {
        try {
            String contentType = getContentType(extension);
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType(contentType)
                    .build();
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(PRESIGNED_URL_MINUTES))
                    .putObjectRequest(putObjectRequest)
                    .build();
            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();
            log.info("Presigned URL 생성 성공 - fileKey: {}", fileKey);
            return presignedUrl;
        } catch (Exception e) {
            log.error("Presigned URL 생성 실패 - fileKey: {}", fileKey, e);
            throw new S3UploadException(fileKey);
        }
    }

    private String generateFileName(String extension) {
        return UUID.randomUUID() + extension;
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isBlank() || !filename.contains(".")) {
            throw new InvalidFileNameException(filename);
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private void validateExtension(String extension) {
        String lowerExtension = extension.toLowerCase();
        if (!lowerExtension.matches("\\.(jpg|jpeg|png|gif)")) {
            throw new InvalidFileExtensionException(extension);
        }
    }

    private String getContentType(String extension) {
        return switch (extension.toLowerCase()) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            default -> "application/octet-stream";
        };
    }
}