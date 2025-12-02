package com.ll.order.domain.messaging.producer;

import com.fasterxml.uuid.Generators;
import com.ll.core.config.kafka.KafkaEventPublisher;
import com.ll.core.model.vo.kafka.InventoryEvent;
import com.ll.core.model.vo.kafka.OrderEvent;
import com.ll.core.model.vo.kafka.RefundEvent;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaEventPublisher kafkaEventPublisher;

    @Retry(name = "orderEventProducer")
    public void sendOrder(OrderEvent event) {
        log.debug("주문 이벤트 발행 시도 - orderItemCode: {}, referenceCode: {}", 
                event.orderItemCode(), event.referenceCode());
        kafkaEventPublisher.publish("order-event", event);
    }

    public void sendRefund(RefundEvent event) {
        kafkaEventPublisher.publish("refund-event", event);
    }

    // 재고 처리는 API 요청으로
    public void sendInventoryDecrease(String productCode, int quantity) {
        String referenceCode = Generators.timeBasedEpochGenerator().generate().toString();
        kafkaEventPublisher.publish("inventory-event", InventoryEvent.stockDecreaseEvent(productCode, quantity, referenceCode));
    }

    public void sendInventoryRollback(String productCode, int quantity) {
        String referenceCode = Generators.timeBasedEpochGenerator().generate().toString();
        kafkaEventPublisher.publish("inventory-event", InventoryEvent.stockRollbackEvent(productCode, quantity, referenceCode));
    }
}
