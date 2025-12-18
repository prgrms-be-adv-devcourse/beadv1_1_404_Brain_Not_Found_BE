package com.ll.products.domain.category.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class CategoryNotFoundException extends BaseException {
    public CategoryNotFoundException(Long id) {
        super(ErrorCode.NOT_FOUND,
                String.format("카테고리를 찾을 수 없습니다. 카테고리id: %d", id)
        );
    }
}
