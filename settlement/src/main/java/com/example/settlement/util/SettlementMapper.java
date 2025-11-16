package com.example.settlement.util;

import com.example.core.model.vo.kafka.OrderEvent;
import com.example.settlement.model.entity.Settlement;

public class SettlementMapper {
    public static Settlement from(OrderEvent event) {
        return Settlement.create(
                event.sellerCode(),
                event.buyerCode(),
                event.orderItemCode(),
                event.referenceCode(),
                event.amount(),
                event.settlementRate()
        );
    }
}
