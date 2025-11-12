package com.ll.products.domain.category.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String message) {
        super(message);
    }
    public CategoryNotFoundException(Long id) {
        super("id가 " + id + "인 카테고리는 존재하지 않습니다.");
    }

}
