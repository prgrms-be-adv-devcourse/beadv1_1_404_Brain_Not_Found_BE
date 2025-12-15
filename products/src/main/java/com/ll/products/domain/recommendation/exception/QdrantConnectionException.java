package com.ll.products.domain.recommendation.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class QdrantConnectionException extends BaseException {
    public QdrantConnectionException() {
        super(ErrorCode.SERVICE_UNAVAILABLE, "Qdrant 연결에 실패했습니다.");
    }
}