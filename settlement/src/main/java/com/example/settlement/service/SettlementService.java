package com.example.settlement.service;

import com.example.core.model.vo.kafka.SettlementRequestEvent;

public interface SettlementService {
    void createSettlement(SettlementRequestEvent event);
}
