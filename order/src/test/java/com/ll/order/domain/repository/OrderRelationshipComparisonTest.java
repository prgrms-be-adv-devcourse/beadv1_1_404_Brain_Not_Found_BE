package com.ll.order.domain.repository;

import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.entity.OrderItem;
import com.ll.order.domain.model.entity.OrderItemWithBidirectional;
import com.ll.order.domain.model.entity.OrderWithBidirectional;
import com.ll.order.domain.model.enums.order.OrderType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 단방향 관계 vs 양방향 관계 성능 비교 테스트
 * 
 * 비교 항목:
 * 1. 단방향 관계: Order와 OrderItem을 각각 별도로 조회 (현재 프로덕션 방식)
 * 2. 양방향 관계: @OneToMany 관계 설정 후 JOIN FETCH로 한 번에 조회
 */
@Slf4j
@DataJpaTest
@ActiveProfiles("test")
class OrderRelationshipComparisonTest {

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private OrderItemJpaRepository orderItemJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final int ORDER_COUNT = 10000;
    private static final int ORDER_ITEM_COUNT_PER_ORDER = 5;

    @BeforeEach
    void setUp() {
        // 단방향 관계 엔티티 데이터 생성
        for (int i = 0; i < ORDER_COUNT; i++) {
            Order order = Order.create(
                    1L,
                    "USER-001",
                    OrderType.ONLINE,
                    "서울시 강남구 테스트동 " + i
            );
            Order savedOrder = orderJpaRepository.save(order);

            for (int j = 0; j < ORDER_ITEM_COUNT_PER_ORDER; j++) {
                OrderItem orderItem = savedOrder.createOrderItem(
                        100L + j,
                        "PROD-" + (100L + j),
                        "SELLER-" + j,
                        "상품명-" + j,
                        j + 1,
                        10000 * (j + 1)
                );
                orderItemJpaRepository.save(orderItem);
            }
        }

        // 양방향 관계 엔티티 데이터 생성
        for (int i = 0; i < ORDER_COUNT; i++) {
            OrderWithBidirectional order = OrderWithBidirectional.create(
                    1L,
                    OrderType.ONLINE,
                    "서울시 강남구 테스트동 " + i
            );
            entityManager.persist(order);

            for (int j = 0; j < ORDER_ITEM_COUNT_PER_ORDER; j++) {
                OrderItemWithBidirectional orderItem = OrderItemWithBidirectional.create(
                        order,
                        100L + j,
                        "SELLER-" + j,
                        "상품명-" + j,
                        j + 1,
                        10000 * (j + 1)
                );
                entityManager.persist(orderItem);
            }
        }

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("성능 비교: 단방향 관계 (각 테이블 조회) vs 양방향 관계 (JOIN FETCH)")
    @Transactional(readOnly = true)
    void compareUnidirectionalVsBidirectional() {
        Session session = entityManager.unwrap(Session.class);
        Statistics statistics = session.getSessionFactory().getStatistics();
        statistics.setStatisticsEnabled(true);

        // ========== 1. 단방향 관계: 각 테이블을 별도로 조회 (현재 방식) ==========
        statistics.clear();
        long unidirectionalStart = System.currentTimeMillis();

        // Order 조회
        List<Order> orders = orderJpaRepository.findAll();
        
        // 각 Order에 대해 OrderItem 조회 (N+1 문제)
        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        List<OrderItem> allOrderItems = orderItemJpaRepository.findByOrderIdIn(orderIds);

        long unidirectionalTime = System.currentTimeMillis() - unidirectionalStart;
        long unidirectionalQueryCount = statistics.getQueryExecutionCount();

        entityManager.clear();
        statistics.clear();

        // ========== 2. 양방향 관계: JOIN FETCH로 한 번에 조회 ==========
        long bidirectionalStart = System.currentTimeMillis();

        String jpql = """
                SELECT DISTINCT o 
                FROM OrderWithBidirectional o 
                LEFT JOIN FETCH o.orderItems
                """;

        List<OrderWithBidirectional> ordersWithItems = entityManager.createQuery(jpql, OrderWithBidirectional.class)
                .getResultList();

        // OrderItem 접근 (이미 로드되어 있음 - 추가 쿼리 없음)
        int totalOrderItems = 0;
        for (OrderWithBidirectional order : ordersWithItems) {
            List<OrderItemWithBidirectional> orderItems = order.getOrderItems();
            totalOrderItems += orderItems.size();
        }

        long bidirectionalTime = System.currentTimeMillis() - bidirectionalStart;
        long bidirectionalQueryCount = statistics.getQueryExecutionCount();

        // ========== 결과 출력 ==========
        log.info("========================================");
        log.info("단방향 관계 vs 양방향 관계 성능 비교");
        log.info("========================================");
        log.info("테스트 데이터: 주문 {}개, 각 주문당 OrderItem {}개", ORDER_COUNT, ORDER_ITEM_COUNT_PER_ORDER);
        log.info("");
        
        log.info("1️⃣ 단방향 관계 (각 테이블 조회)");
        log.info("   - 실행 시간: {} ms", unidirectionalTime);
        log.info("   - 쿼리 개수: {} 개 (Order 조회 1개 + OrderItem IN 절 조회 1개)", unidirectionalQueryCount);
        log.info("   - 조회된 Order: {} 개", orders.size());
        log.info("   - 조회된 OrderItem: {} 개", allOrderItems.size());
        log.info("");
        
        log.info("2️⃣ 양방향 관계 (JOIN FETCH)");
        log.info("   - 실행 시간: {} ms", bidirectionalTime);
        log.info("   - 쿼리 개수: {} 개 (JOIN FETCH 1개)", bidirectionalQueryCount);
        log.info("   - 조회된 Order: {} 개", ordersWithItems.size());
        log.info("   - 조회된 OrderItem: {} 개", totalOrderItems);
        log.info("");
        
        // 성능 비교
        if (unidirectionalTime > 0 && bidirectionalTime > 0) {
            double timeImprovement = ((double)(unidirectionalTime - bidirectionalTime) / unidirectionalTime) * 100;
            double queryReduction = ((double)(unidirectionalQueryCount - bidirectionalQueryCount) / unidirectionalQueryCount) * 100;
            
            log.info("📊 성능 비교 결과");
            log.info("   - 실행 시간 개선율: {}%", String.format("%.2f", timeImprovement));
            log.info("   - 쿼리 개수 감소율: {}%", String.format("%.2f", queryReduction));
            log.info("");
            
            if (timeImprovement > 0) {
                log.info("✅ 양방향 관계가 {}% 더 빠릅니다", String.format("%.2f", timeImprovement));
            } else {
                log.info("⚠️ 단방향 관계가 {}% 더 빠릅니다", String.format("%.2f", -timeImprovement));
            }
        }
        
        log.info("========================================");
        log.info("결론");
        log.info("========================================");
        log.info("✅ 양방향 관계 + JOIN FETCH: {}번의 쿼리로 모든 데이터 조회", bidirectionalQueryCount);
        log.info("📋 단방향 관계: {}번의 쿼리로 모든 데이터 조회", unidirectionalQueryCount);
        log.info("");
        log.info("💡 양방향 관계의 장점:");
        log.info("   - 1번의 쿼리로 모든 데이터 조회 가능");
        log.info("   - order.getOrderItems() 접근 시 추가 쿼리 없음");
        log.info("");
        log.info("💡 단방향 관계의 장점:");
        log.info("   - 명확한 의존성 (OrderItem → Order만 존재)");
        log.info("   - 메모리 효율적 (필요한 데이터만 조회)");
        log.info("   - 순환 참조 문제 없음");
    }

    @Test
    @DisplayName("단방향 관계: N+1 문제 발생 케이스")
    @Transactional(readOnly = true)
    void testUnidirectionalNPlusOne() {
        Session session = entityManager.unwrap(Session.class);
        Statistics statistics = session.getSessionFactory().getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        long startTime = System.currentTimeMillis();

        // Order 조회
        List<Order> orders = orderJpaRepository.findAll();

        // 각 Order에 대해 OrderItem 조회 (N+1 문제 발생)
        for (Order order : orders) {
            List<OrderItem> orderItems = orderItemJpaRepository.findByOrderId(order.getId());
            assertThat(orderItems).isNotEmpty();
        }

        long executionTime = System.currentTimeMillis() - startTime;
        long queryCount = statistics.getQueryExecutionCount();

        log.info("=== 단방향 관계 (N+1 문제) ===");
        log.info("실행 시간: {} ms", executionTime);
        log.info("총 쿼리 개수: {} 개 (Order 1개 + OrderItem {}개)", queryCount, orders.size());
        log.info("⚠️ N+1 문제: 각 Order마다 별도의 쿼리 발생");
    }

    @Test
    @DisplayName("양방향 관계: LAZY 로딩 (JOIN FETCH 없이)")
    @Transactional(readOnly = true)
    void testBidirectionalLazyLoading() {
        Session session = entityManager.unwrap(Session.class);
        Statistics statistics = session.getSessionFactory().getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        long startTime = System.currentTimeMillis();

        // JOIN FETCH 없이 조회
        String jpql = """
                SELECT o 
                FROM OrderWithBidirectional o
                """;

        List<OrderWithBidirectional> orders = entityManager.createQuery(jpql, OrderWithBidirectional.class)
                .getResultList();

        // OrderItem 접근 시 LAZY 로딩 발생 (N+1 문제)
        for (OrderWithBidirectional order : orders) {
            List<OrderItemWithBidirectional> orderItems = order.getOrderItems();
            assertThat(orderItems).isNotEmpty();
        }

        long executionTime = System.currentTimeMillis() - startTime;
        long queryCount = statistics.getQueryExecutionCount();
        long collectionLoadCount = statistics.getCollectionLoadCount();
        long totalQueries = queryCount + collectionLoadCount;

        log.info("=== 양방향 관계 (LAZY 로딩, N+1 문제) ===");
        log.info("실행 시간: {} ms", executionTime);
        log.info("SELECT 쿼리: {} 개", queryCount);
        log.info("Collection 로드 쿼리: {} 개", collectionLoadCount);
        log.info("총 쿼리 개수: {} 개", totalQueries);
        log.info("⚠️ N+1 문제: 각 Order의 orderItems 접근 시마다 별도의 쿼리 발생");
    }

    @Test
    @DisplayName("종합 비교: 모든 방식 성능 비교")
    @Transactional(readOnly = true)
    void comprehensiveComparison() {
        Session session = entityManager.unwrap(Session.class);
        Statistics statistics = session.getSessionFactory().getStatistics();
        statistics.setStatisticsEnabled(true);

        log.info("========================================");
        log.info("종합 성능 비교 테스트");
        log.info("========================================");
        log.info("");

        // 1. 단방향 관계 (N+1 문제)
        statistics.clear();
        long time1 = measureTime(() -> {
            List<Order> orders = orderJpaRepository.findAll();
            for (Order order : orders) {
                orderItemJpaRepository.findByOrderId(order.getId());
            }
        });
        long queries1 = statistics.getQueryExecutionCount();
        log.info("1. 단방향 관계 (N+1): {} ms, 쿼리 {} 개", time1, queries1);

        // 2. 단방향 관계 (IN 절 개선)
        entityManager.clear();
        statistics.clear();
        long time2 = measureTime(() -> {
            List<Order> orders = orderJpaRepository.findAll();
            List<Long> orderIds = orders.stream().map(Order::getId).toList();
            orderItemJpaRepository.findByOrderIdIn(orderIds);
        });
        long queries2 = statistics.getQueryExecutionCount();
        log.info("2. 단방향 관계 (IN 절): {} ms, 쿼리 {} 개", time2, queries2);

        // 3. 양방향 관계 (JOIN FETCH)
        entityManager.clear();
        statistics.clear();
        long time3 = measureTime(() -> {
            String jpql = """
                    SELECT DISTINCT o 
                    FROM OrderWithBidirectional o 
                    LEFT JOIN FETCH o.orderItems
                    """;
            List<OrderWithBidirectional> orders = entityManager.createQuery(jpql, OrderWithBidirectional.class)
                    .getResultList();
            for (OrderWithBidirectional order : orders) {
                order.getOrderItems().size();
            }
        });
        long queries3 = statistics.getQueryExecutionCount();
        log.info("3. 양방향 관계 (JOIN FETCH): {} ms, 쿼리 {} 개", time3, queries3);

        // 4. 양방향 관계 (LAZY 로딩)
        entityManager.clear();
        statistics.clear();
        long time4 = measureTime(() -> {
            String jpql = """
                    SELECT o 
                    FROM OrderWithBidirectional o
                    """;
            List<OrderWithBidirectional> orders = entityManager.createQuery(jpql, OrderWithBidirectional.class)
                    .getResultList();
            for (OrderWithBidirectional order : orders) {
                order.getOrderItems().size();
            }
        });
        long queries4 = statistics.getQueryExecutionCount();
        long collectionLoad4 = statistics.getCollectionLoadCount();
        log.info("4. 양방향 관계 (LAZY): {} ms, 쿼리 {} 개 (SELECT {}개 + Collection {}개)", 
                time4, queries4 + collectionLoad4, queries4, collectionLoad4);

        log.info("");
        log.info("========================================");
        log.info("결론");
        log.info("========================================");
        log.info("🏆 최고 성능: 양방향 관계 (JOIN FETCH) - {} ms, {} 개 쿼리", time3, queries3);
        log.info("📈 단방향 관계 개선: IN 절 사용 시 {} ms, {} 개 쿼리", time2, queries2);
        log.info("❌ 최악 성능: N+1 문제 발생 방식 - {} ms, {} 개 쿼리", time1, queries1);
    }

    private long measureTime(Runnable task) {
        long start = System.currentTimeMillis();
        task.run();
        return System.currentTimeMillis() - start;
    }
}

