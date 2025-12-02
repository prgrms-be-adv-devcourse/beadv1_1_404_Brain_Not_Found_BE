package com.ll.products.domain.product.model.entity;

public enum DlqStatus {
    PENDING,    // 수동 처리 대기
    PROCESSED,  // 처리 완료
    FAILED      // 처리 실패
}
