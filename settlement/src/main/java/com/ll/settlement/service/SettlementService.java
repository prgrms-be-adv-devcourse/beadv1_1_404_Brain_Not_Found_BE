package com.ll.settlement.service;

import com.ll.core.model.vo.kafka.RefundEvent;
import com.ll.core.model.vo.kafka.SettlementEvent;
import com.ll.core.model.vo.kafka.OrderEvent;

public interface SettlementService {
    void createSettlement(OrderEvent event);
    void failByDlqEvent(SettlementEvent event);
    void refundSettlement(RefundEvent event);
}
