package com.ll.settlement.batch.proccessor;

import com.ll.settlement.model.entity.Settlement;
import com.ll.settlement.model.exception.SkippableItemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@StepScope
@RequiredArgsConstructor
@Component("settlementProcessor")
public class SettlementProcessor implements ItemProcessor<Settlement, Settlement> {
    @Override
    public Settlement process(Settlement settlement) {
        try {
            settlement.done();
            return settlement;
        } catch ( Exception e ) {
            log.warn("WILL BE SKIPPED (via policy) | settlementId={} | reason={}", settlement.getId(), e.getMessage());
            throw new SkippableItemException(e.getMessage());
        }
    }
}
