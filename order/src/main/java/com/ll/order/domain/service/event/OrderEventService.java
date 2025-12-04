package com.ll.order.domain.service.event;

import com.ll.core.model.vo.kafka.OrderEvent;
import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventService {

    private final OrderEventOutboxService orderEventOutboxService;

    public void publishOrderCompletedEvents(Order order, List<OrderItem> orderItems, String buyerCode) {
        for (OrderItem orderItem : orderItems) {
            OrderEvent orderEvent = OrderEvent.of(
                    buyerCode,
                    orderItem.getSellerCode(),
                    orderItem.getCode(),
                    order.getCode(),
                    (long) orderItem.getPrice() * orderItem.getQuantity()
            );

            // Outbox 패턴: 트랜잭션 내에서 먼저 Outbox에 저장 (PENDING 상태)
            // 별도 프로세스가 Outbox를 읽어서 Kafka에 발행
            orderEventOutboxService.saveToOutbox(orderEvent, order.getCode(), orderItem.getCode());
        }
    }
}

