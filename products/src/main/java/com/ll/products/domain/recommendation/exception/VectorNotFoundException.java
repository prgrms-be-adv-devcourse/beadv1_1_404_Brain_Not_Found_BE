package com.ll.products.domain.recommendation.exception;

import com.ll.core.model.exception.BaseErrorCode;
import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class VectorNotFoundException extends BaseException {
    public VectorNotFoundException(String code) {
        super(ErrorCode.NOT_FOUND,
                String.format("벡터DB에서 상품을 찾을 수 없습니다. 상품코드: %s", code));
    }

    public VectorNotFoundException() {
        super(ErrorCode.NOT_FOUND, "조회 가능한 벡터가 없습니다.");
    }
}
