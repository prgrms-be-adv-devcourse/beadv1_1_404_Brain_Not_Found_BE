package com.ll.payment.settlement.service;

import com.ll.core.model.vo.kafka.OrderEvent;
import com.ll.core.model.vo.kafka.RefundEvent;

public interface SettlementService {
    void createSettlement(OrderEvent event);
    void refundSettlement(RefundEvent event);
}
