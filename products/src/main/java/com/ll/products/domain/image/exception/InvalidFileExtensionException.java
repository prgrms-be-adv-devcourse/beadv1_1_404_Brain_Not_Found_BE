package com.ll.products.domain.image.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class InvalidFileExtensionException extends BaseException {
    public InvalidFileExtensionException(String extension) {
        super(ErrorCode.BAD_REQUEST, String.format("%s는 유효하지 않은 파일 형식입니다. 가능한 파일 형식: jpg, jpeg, png, gif", extension));
    }
}
