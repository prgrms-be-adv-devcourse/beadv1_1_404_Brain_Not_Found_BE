package com.ll.order.domain.messaging.producer;

import com.ll.core.config.kafka.KafkaEventPublisher;
import com.ll.core.model.vo.kafka.InventoryEvent;
import com.ll.core.model.vo.kafka.OrderEvent;
//import com.ll.core.model.vo.kafka.RefundEvent;
import com.ll.core.model.vo.kafka.PaymentRefundRequestEvent;
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

//    public void sendRefund(RefundEvent event) {
//        kafkaEventPublisher.publish("refund-event", event);
//    }

    public void sendPaymentRefundRequest(PaymentRefundRequestEvent event) {
        log.debug("환불 요청 이벤트 발행 시도 - orderCode: {}, orderId: {}", 
                event.orderCode(), event.orderId());
        kafkaEventPublisher.publish("payment-refund-request-event", event);
    }

    public void sendInventoryRollback(String productCode, int quantity, String referenceCode) {
        kafkaEventPublisher.publish("inventory-event", InventoryEvent.stockRollbackEvent(productCode, quantity, referenceCode));
    }
}
