package com.example.settlement.batch.writer;

import com.example.core.model.vo.kafka.SettlementEvent;
import com.example.settlement.messaging.producer.SettlementEventProducer;
import com.example.settlement.model.entity.Settlement;
import com.example.settlement.repository.SettlementRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component("settlementWriter")
public class SettlementWriter implements ItemWriter<Settlement> {

    @PersistenceContext
    private EntityManager em;
    private final SettlementRepository settlementRepository;
    private final SettlementEventProducer settlementEventProducer;

    @Override
    public void write(Chunk<? extends Settlement> chunk) {
        try {
            settlementRepository.saveAll(chunk.getItems());
            settlementRepository.flush();
            em.clear();
            chunk.getItems().forEach(this::publishKafkaEvent);
        } catch (Exception e) {
            log.error("Error occurred while writing settlements: {}", e.getMessage(), e);
            chunk.getItems().forEach(settlement -> settlement.fail("정산 작업 진행 중 SettlementWriter 에서 에러 발생: " + e.getMessage()));
            settlementRepository.saveAll(chunk.getItems());
            settlementRepository.flush();
        }
    }

    private void publishKafkaEvent(Settlement settlement) {
        SettlementEvent event = SettlementEvent.from(
                settlement.getId(),
                settlement.getSellerCode(),
                settlement.getOrderItemCode(),
                settlement.getSettlementBalance()
        );
        log.info("Publishing SettlementCompleteEvent: {}", event);
        settlementEventProducer.send(event);
    }

}
