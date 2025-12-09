package com.ll.payment.settlement.batch.writer;

import com.ll.payment.settlement.model.entity.Settlement;
import com.ll.payment.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component("settlementWriter")
public class SettlementWriter implements ItemWriter<Settlement> {

    private final SettlementRepository settlementRepository;

    @Override
    public void write(Chunk<? extends Settlement> chunk) {
        chunk.forEach(settlementRepository::save);
    }

}
