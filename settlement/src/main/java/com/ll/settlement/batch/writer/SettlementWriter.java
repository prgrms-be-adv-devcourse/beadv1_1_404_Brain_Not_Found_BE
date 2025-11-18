package com.ll.settlement.batch.writer;

import com.ll.core.model.vo.kafka.SettlementEvent;
import com.ll.settlement.messaging.producer.SettlementEventProducer;
import com.ll.settlement.model.entity.Settlement;
import com.ll.settlement.repository.SettlementRepository;
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
        settlementEventProducer.sendSettlement(event);
    }

}
