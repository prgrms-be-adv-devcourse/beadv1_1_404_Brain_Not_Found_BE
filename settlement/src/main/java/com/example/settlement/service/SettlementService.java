package com.example.settlement.service;

import com.example.core.model.vo.kafka.SettlementEvent;
import com.example.core.model.vo.kafka.OrderEvent;

public interface SettlementService {
    void createSettlement(OrderEvent event);
    void failByDlqEvent(SettlementEvent event);
}
