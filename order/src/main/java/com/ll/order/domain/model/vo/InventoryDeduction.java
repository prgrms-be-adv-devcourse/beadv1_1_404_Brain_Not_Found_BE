package com.ll.order.domain.model.vo;

/**
 * 재고 차감 정보를 저장하기 위한 레코드
 * 재고 롤백 시 사용됨
 */
public record InventoryDeduction(
        String productCode,
        Integer quantity
) {
}

