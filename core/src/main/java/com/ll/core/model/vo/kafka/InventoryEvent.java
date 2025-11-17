package com.ll.core.model.vo.kafka;

import com.ll.core.model.vo.kafka.enums.InventoryEventType;

public record InventoryEvent(
        String productCode,
        int quantity,
        InventoryEventType eventType,
        String referenceCode // 중복 방지
){}
