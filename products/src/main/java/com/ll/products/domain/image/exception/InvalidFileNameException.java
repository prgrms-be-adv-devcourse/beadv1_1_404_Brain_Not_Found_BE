package com.ll.products.domain.image.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class InvalidFileNameException extends BaseException {
    public InvalidFileNameException(String name) {
        super(ErrorCode.BAD_REQUEST, String.format("유효하지 않은 파일명입니다. 파일명: %s", name));
    }
}
