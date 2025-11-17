package com.ll.order.config;

import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.entity.OrderItem;
import com.ll.order.domain.model.enums.OrderStatus;
import com.ll.order.domain.model.enums.OrderType;
import com.ll.order.domain.repository.OrderItemJpaRepository;
import com.ll.order.domain.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
//@Component
@RequiredArgsConstructor
@Profile("!prod") // 프로덕션 환경에서는 실행되지 않도록
public class DataInitializer implements CommandLineRunner {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderItemJpaRepository orderItemJpaRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (orderJpaRepository.count() > 0) {
            log.info("더미 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        log.info("더미 주문 데이터를 생성합니다...");

        // 테스트용 주문 1 (생성됨 상태, 단일 상품)
        Order order1 = Order.create("ORD-TEST-001", 1L, OrderType.ONLINE, "서울시 강남구 테헤란로 123");
        order1 = orderJpaRepository.save(order1);
        
        OrderItem orderItem1 = order1.createOrderItem(
                1L, // productId
                3L, // sellerId
                "테스트 상품 1",
                2, // quantity
                10000 // pricePerUnit
        );
        orderItemJpaRepository.save(orderItem1);
        log.info("주문 생성 완료: orderCode={}, totalPrice={}", order1.getOrderCode(), order1.getTotalPrice());

        // 테스트용 주문 2 (결제 완료 상태, 단일 상품)
        Order order2 = Order.create("ORD-TEST-002", 1L, OrderType.ONLINE, "서울시 서초구 서초대로 456");
        order2.changeStatus(OrderStatus.PAID);
        order2 = orderJpaRepository.save(order2);
        
        OrderItem orderItem2 = order2.createOrderItem(
                2L, // productId
                3L, // sellerId
                "테스트 상품 2",
                1, // quantity
                20000 // pricePerUnit
        );
        orderItemJpaRepository.save(orderItem2);
        log.info("주문 생성 완료: orderCode={}, status={}, totalPrice={}", 
                order2.getOrderCode(), order2.getOrderStatus(), order2.getTotalPrice());

        // 테스트용 주문 3 (다중 상품)
        Order order3 = Order.create("ORD-TEST-003", 2L, OrderType.ONLINE, "서울시 마포구 홍대입구로 789");
        order3 = orderJpaRepository.save(order3);
        
        OrderItem orderItem3_1 = order3.createOrderItem(
                1L, // productId
                3L, // sellerId
                "테스트 상품 1",
                1, // quantity
                10000 // pricePerUnit
        );
        orderItemJpaRepository.save(orderItem3_1);
        
        OrderItem orderItem3_2 = order3.createOrderItem(
                2L, // productId
                3L, // sellerId
                "테스트 상품 2",
                1, // quantity
                20000 // pricePerUnit
        );
        orderItemJpaRepository.save(orderItem3_2);
        log.info("주문 생성 완료: orderCode={}, totalPrice={}", order3.getOrderCode(), order3.getTotalPrice());

        log.info("더미 주문 데이터 생성 완료. 총 {}개", orderJpaRepository.count());
    }
}

