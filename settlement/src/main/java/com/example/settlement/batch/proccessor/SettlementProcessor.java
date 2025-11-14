package com.example.settlement.batch.proccessor;

import com.example.core.model.vo.kafka.SettlementCompleteEvent;
import com.example.settlement.messaging.producer.SettlementEventProducer;
import com.example.settlement.model.entity.Settlement;
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

    private final SettlementEventProducer settlementEventProducer;

    @Override
    public Settlement process(Settlement settlement) {
        settlement.done();
        settlementEventProducer.send(SettlementCompleteEvent.settlementFrom(
                settlement.getSellerCode(),
                settlement.getOrderItemCode(),
                settlement.getSettlementBalance()
        ));
        return settlement;
    }

}
