package com.ll.payment.settlement.service;

import com.ll.core.model.vo.kafka.OrderEvent;
import com.ll.core.model.vo.kafka.RefundEvent;
import com.ll.core.model.vo.kafka.SettlementEvent;

public interface SettlementService {
    void createSettlement(OrderEvent event);
    void failByDlqEvent(SettlementEvent event);
    void refundSettlement(RefundEvent event);
}
