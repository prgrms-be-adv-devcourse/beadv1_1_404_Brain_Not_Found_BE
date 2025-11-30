package com.ll.products.domain.s3.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class S3UploadException extends BaseException {
    public S3UploadException(String fileName) {
        super(ErrorCode.INTERNAL_SERVER_ERROR, String.format("이미지 업로드에 실패했습니다: %s", fileName));
    }
}