package com.example.settlement.service;

import com.example.core.model.vo.kafka.SettlementRequestEvent;
import com.example.settlement.model.entity.Settlement;
import com.example.settlement.repository.SettlementRepository;
import com.example.settlement.util.SettlementMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;

    @Override
    public void createSettlement(SettlementRequestEvent event) {
        Settlement settlement = SettlementMapper.from(event);
        settlementRepository.save(settlement);
        log.info("Created settlement : {}", settlement);
    }

}
