package com.ll.products.domain.cart.model.enums;

public enum CartStatus {
    ACTIVE,          // 담기/수정 중인 기본 장바구니
    CHECKOUT_READY,  // 주문 직전, 검증 대기 상태
    PURCHASED,       // 주문으로 전환 및 결제 완료
    CANCELLED        // 사용자 취소 또는 실패로 더 이상 사용하지 않음
}
