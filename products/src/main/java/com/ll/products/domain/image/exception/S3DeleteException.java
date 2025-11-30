package com.ll.products.domain.image.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class S3DeleteException extends BaseException {
    public S3DeleteException(String fileKey) {
        super(ErrorCode.INTERNAL_SERVER_ERROR, String.format("이미지 삭제에 실패했습니다: %s", fileKey));
    }
}