package com.ll.payment.settlement.util;

import com.ll.core.model.vo.kafka.OrderEvent;
import com.ll.payment.settlement.model.entity.Settlement;

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
