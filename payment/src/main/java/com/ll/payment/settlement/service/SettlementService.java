package com.ll.payment.settlement.service;

import com.ll.core.model.vo.kafka.OrderEvent;
import com.ll.core.model.vo.kafka.PaymentRefundRequestEvent;

public interface SettlementService {
    void createSettlement(OrderEvent event);
    void refundSettlement(PaymentRefundRequestEvent event);
}
