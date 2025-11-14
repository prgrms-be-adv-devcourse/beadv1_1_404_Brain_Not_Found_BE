package com.example.settlement.batch.writer;

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


    @Override
    public void write(Chunk<? extends Settlement> chunk) {
        settlementRepository.saveAll(chunk.getItems());
        settlementRepository.flush();
        em.clear();
    }

}
