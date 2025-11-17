package com.ll.core.model.vo.kafka;

import com.ll.core.model.vo.kafka.enums.InventoryEventType;

public record InventoryEvent(
        String productCode,
        int quantity,
        InventoryEventType eventType,
        String referenceCode // 중복 방지
){
    public static InventoryEvent stockDecreaseEvent(String productCode, int quantity, String referenceCode) {
        return new InventoryEvent(
                productCode,
                quantity,
                InventoryEventType.STOCK_DECREMENT,
                referenceCode
        );
    }

    public static InventoryEvent stockRollbackEvent(String productCode, int quantity, String referenceCode) {
        return new InventoryEvent(
                productCode,
                quantity,
                InventoryEventType.STOCK_ROLLBACK,
                referenceCode
        );
    }

}
