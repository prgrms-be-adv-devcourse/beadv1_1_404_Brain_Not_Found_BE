package com.ll.products.domain.category.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class CategoryPermissionException extends BaseException {
    public CategoryPermissionException() {
        super(ErrorCode.FORBIDDEN,
                String.format("카테고리 권한이 없습니다. 관리자만 가능합니다."));
    }
}