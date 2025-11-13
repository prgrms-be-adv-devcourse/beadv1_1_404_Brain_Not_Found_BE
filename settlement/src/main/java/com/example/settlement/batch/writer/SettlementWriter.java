package com.example.settlement.batch.writer;

import com.example.settlement.messaging.producer.SettlementEventProducer;
import com.example.settlement.model.entity.Settlement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementWriter implements ItemWriter<Settlement> {

    private final SettlementEventProducer settlementEventProducer;

    @Override
    public void write(Chunk<? extends Settlement> chunk) throws Exception {

        chunk.getItems().forEach(settlement -> {
            try {
                settlementEventProducer.send(settlement);
                settlement.done();
            } catch (Exception e) {
                log.error("Failed to write settlement for ID: {}", settlement.getId(), e);
            }
        });

    }
}
