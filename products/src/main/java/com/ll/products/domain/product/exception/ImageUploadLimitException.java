package com.ll.products.domain.product.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class ImageUploadLimitException extends BaseException {
    public ImageUploadLimitException(int maxImageSize) {
        super(ErrorCode.BAD_REQUEST,
                String.format("최대 이미지 수량을 초과했습니다. 최대 이미지 수량: %d", maxImageSize));
    }

    public ImageUploadLimitException() {
        super(ErrorCode.BAD_REQUEST, "메인 이미지는 하나만 지정 가능합니다");
    }
}
