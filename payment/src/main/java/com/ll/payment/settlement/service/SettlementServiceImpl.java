package com.ll.payment.settlement.service;

import com.ll.core.model.vo.kafka.OrderEvent;
import com.ll.core.model.vo.kafka.RefundEvent;
import com.ll.payment.settlement.model.exception.SettlementNotFoundException;
import com.ll.payment.settlement.model.entity.Settlement;
import com.ll.payment.settlement.repository.SettlementRepository;
import com.ll.payment.settlement.util.SettlementMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;

    @Override
    public void createSettlement(OrderEvent event) {
        Settlement settlement = SettlementMapper.from(event);
        settlementRepository.save(settlement);
    }

    @Override
    public void refundSettlement(RefundEvent event) {
        Settlement settlement = settlementRepository.findByOrderItemCode(event.orderItemCode()).orElseThrow(SettlementNotFoundException::new);
        settlement.refund();
        settlementRepository.save(settlement);
    }

}
