package com.ll.settlement.service;

import com.ll.core.model.vo.kafka.SettlementEvent;
import com.ll.core.model.vo.kafka.OrderEvent;
import com.ll.settlement.messaging.producer.SettlementEventProducer;
import com.ll.settlement.model.entity.Settlement;
import com.ll.settlement.model.exception.SettlementNotFoundException;
import com.ll.settlement.repository.SettlementRepository;
import com.ll.settlement.util.SettlementMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;
    private final SettlementEventProducer settlementEventProducer;

    @Override
    public void createSettlement(OrderEvent event) {
        Settlement settlement = SettlementMapper.from(event);
        settlementRepository.save(settlement);
        log.info("Created settlement : {}", settlement);
        settlementEventProducer.sendOrder(OrderEvent.fromSettlementComplete(event));
    }

    @Override
    public void failByDlqEvent(SettlementEvent event) {
        Settlement settlement = settlementRepository.findById(event.settlementId()).orElseThrow(SettlementNotFoundException::new);
        settlement.fail("Settlement failed due to external error");
        settlementRepository.save(settlement);
    }
}
