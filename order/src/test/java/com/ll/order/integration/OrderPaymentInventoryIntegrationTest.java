package com.ll.order.integration;

import com.ll.order.domain.client.CartServiceClient;
import com.ll.order.domain.client.PaymentServiceClient;
import com.ll.order.domain.client.ProductServiceClient;
import com.ll.order.domain.client.UserServiceClient;
import com.ll.order.domain.messaging.producer.OrderEventProducer;
import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.entity.OrderItem;
import com.ll.order.domain.model.entity.history.OrderHistoryEntity;
import com.ll.order.domain.model.enums.order.OrderHistoryActionType;
import com.ll.order.domain.model.enums.order.OrderStatus;
import com.ll.order.domain.model.enums.order.OrderType;
import com.ll.order.domain.model.enums.payment.PaidType;
import com.ll.order.domain.model.enums.product.ProductStatus;
import com.ll.order.domain.model.enums.user.AccountStatus;
import com.ll.order.domain.model.enums.user.Grade;
import com.ll.order.domain.model.enums.user.Role;
import com.ll.order.domain.model.enums.user.SocialProvider;
import com.ll.order.domain.model.vo.request.OrderDirectRequest;
import com.ll.order.domain.model.vo.request.OrderPaymentRequest;
import com.ll.order.domain.model.vo.response.order.OrderCreateResponse;
import com.ll.order.domain.model.vo.response.product.ProductResponse;
import com.ll.order.domain.model.vo.response.user.UserResponse;
import com.ll.order.domain.repository.OrderHistoryJpaRepository;
import com.ll.order.domain.repository.OrderItemJpaRepository;
import com.ll.order.domain.repository.OrderJpaRepository;
import com.ll.order.domain.service.compensation.CompensationService;
import com.ll.order.domain.service.event.OrderEventService;
import com.ll.order.domain.service.order.OrderService;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("주문-결제-재고 통합 테스트")
@Slf4j
class OrderPaymentInventoryIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private OrderItemJpaRepository orderItemJpaRepository;

    @Autowired
    private OrderHistoryJpaRepository orderHistoryJpaRepository;

    // 외부 서비스 모킹 (다른 마이크로서비스)
    @MockitoBean
    private UserServiceClient userServiceClient;

    @MockitoBean
    private ProductServiceClient productServiceClient;

    @MockitoBean
    private PaymentServiceClient paymentServiceClient;

    @MockitoBean
    private CartServiceClient cartServiceClient;

    // ========== 통합 테스트 전략 ==========
    // ✅ 실제 사용: OrderService, Repository, OrderValidator, OrderInventoryService
    // ✅ 모킹: 외부 마이크로서비스 (User, Product, Payment, Cart)
    // ✅ 모킹: 이벤트/메시징 (Kafka 등 외부 인프라)

    // 이벤트/메시징 관련 - 모킹 (외부 인프라: Kafka 등)
    @MockitoBean
    private OrderEventProducer orderEventProducer;

    @MockitoBean
    private CompensationService compensationService;

    @MockitoBean
    private OrderEventService orderEventService;

    // OrderValidator, OrderInventoryService는 실제 빈 사용
    // (Spring이 자동으로 주입하므로 @Autowired 불필요)

    // 테스트 데이터
    private UserResponse testUser;
    private ProductResponse testProduct;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 설정
        testUser = new UserResponse(
                1L,
                "USER-001",
                "test_social_id",
                SocialProvider.KAKAO,
                "test@test.com",
                "홍길동",
                Role.USER,
                null,
                5L,
                Grade.BRONZE,
                AccountStatus.ACTIVE,
                null,
                null,
                null,
                null
        );

        // 테스트 상품 설정 (재고: 10개)
        testProduct = ProductResponse.builder()
                .id(1L)
                .code("PROD-001")
                .name("테스트상품")
                .sellerCode("SELLER-001")
                .sellerName("판매자1")
                .quantity(10) // 초기 재고
                .price(10000)
                .status(ProductStatus.ON_SALE)
                .images(null)
                .build();
    }

    // ========== 다이렉트 주문 통합 테스트 ==========

    /**
     * 통합 테스트: 다이렉트 주문 생성 - 예치금 결제 (DEPOSIT)
     * <p>
     * 검증 항목:
     * 1. 실제 DB에 주문이 저장되는지 확인
     * 2. 실제 OrderValidator가 재고 검증을 수행하는지 확인
     * 3. 실제 트랜잭션이 동작하는지 확인
     * 4. 외부 서비스 호출이 올바른지 확인
     * 5. 예치금 결제가 성공하고 주문 상태가 COMPLETED로 변경되는지 확인
     */
    @DisplayName("통합 테스트: 다이렉트 주문 생성 - 예치금 결제 (DEPOSIT)")
    @Test
    @Transactional
    void createDirectOrder_DepositPayment() {
        // given
        log.info("*****************************before createDirectOrder*****************************");
        String userCode = "USER-001";
        OrderDirectRequest request = new OrderDirectRequest(
                "PROD-001",
                2,
                "서울시 강남구",
                OrderType.ONLINE,
                PaidType.DEPOSIT,
                null
        );

        // 외부 서비스 Mock 설정 (다른 마이크로서비스)
        when(userServiceClient.getUserByCode("USER-001")).thenReturn(testUser);
        when(productServiceClient.getProductByCode("PROD-001")).thenReturn(testProduct);
        doNothing().when(productServiceClient).decreaseInventory(anyString(), any());
        when(paymentServiceClient.requestDepositPayment(any())).thenReturn("OK");

        // when - 실제 OrderService 호출 (실제 DB 사용)
        OrderCreateResponse result = orderService.createDirectOrder(request, userCode);
        log.info("*****************************after createDirectOrder*****************************");
        // then - 통합 테스트 검증
        // 1. 응답 검증
        log.info("*****************************응답 검증*****************************");
        assertThat(result).isNotNull();
        assertThat(result.orderCode()).isNotNull();
        assertThat(result.buyerId()).isEqualTo(1L);
        assertThat(result.totalPrice()).isEqualTo(20000); // 2개 * 10000원
        assertThat(result.orderStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(result.orderType()).isEqualTo(OrderType.ONLINE);
        assertThat(result.address()).isEqualTo("서울시 강남구");
        assertThat(result.orderItems()).hasSize(1);

        // 2. 실제 DB에 저장되었는지 확인 (통합 테스트의 핵심!)
        Order savedOrder = orderJpaRepository.findByCode(result.orderCode());
        log.info("*****************************savedOrder*****************************");
        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(savedOrder.getTotalPrice()).isEqualTo(20000);
        assertThat(savedOrder.getBuyerId()).isEqualTo(1L);
        assertThat(savedOrder.getBuyerCode()).isEqualTo("USER-001");

        // 3. 실제 DB에 주문 항목이 저장되었는지 확인
        List<OrderItem> orderItems = orderItemJpaRepository.findByOrderId(savedOrder.getId());
        log.info("*****************************orderItems*****************************");
        assertThat(orderItems).hasSize(1);
        assertThat(orderItems.getFirst().getQuantity()).isEqualTo(2);
        assertThat(orderItems.getFirst().getPrice()).isEqualTo(10000);
        assertThat(orderItems.getFirst().getProductCode()).isEqualTo("PROD-001");
        assertThat(orderItems.getFirst().getProductId()).isEqualTo(1L);

        // 4. 실제 DB에 주문 이력이 저장되었는지 확인
        // 주문 생성 시: CREATE 이력 저장
        // 결제 성공 시: STATUS_CHANGE 이력 저장 (COMPLETED 상태로 변경)
        List<OrderHistoryEntity> orderHistories = orderHistoryJpaRepository.findByOrderId(savedOrder.getId());
        log.info("*****************************orderHistories*****************************");
        assertThat(orderHistories).hasSizeGreaterThanOrEqualTo(2); // 최소 2개 이상 (생성 + 결제 성공)
        
        // 주문 생성 이력 확인
        OrderHistoryEntity createHistory = orderHistories.stream()
                .filter(history -> history.getActionType() == OrderHistoryActionType.CREATE)
                .findFirst()
                .orElse(null);
        log.info("*****************************createHistory*****************************");
        assertThat(createHistory).isNotNull();
        assertThat(createHistory.getOrderCode()).isEqualTo(savedOrder.getCode());
        assertThat(createHistory.getCurrentStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(createHistory.getPreviousStatus()).isNull(); // 처음 생성이므로 이전 상태 없음
        assertThat(createHistory.getRelatedOrderItemIds()).hasSize(1);
        assertThat(createHistory.getRelatedOrderItemIds().get(0)).isEqualTo(orderItems.get(0).getId());
        
        // 결제 성공 이력 확인
        OrderHistoryEntity paymentSuccessHistory = orderHistories.stream()
                .filter(history -> history.getActionType() == OrderHistoryActionType.STATUS_CHANGE)
                .filter(history -> history.getCurrentStatus() == OrderStatus.COMPLETED)
                .findFirst()
                .orElse(null);
        log.info("*****************************paymentSuccessHistory*****************************");
        assertThat(paymentSuccessHistory).isNotNull();
        assertThat(paymentSuccessHistory.getOrderCode()).isEqualTo(savedOrder.getCode());
        assertThat(paymentSuccessHistory.getCurrentStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(paymentSuccessHistory.getPreviousStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(paymentSuccessHistory.getReason()).contains("예치금");
        assertThat(paymentSuccessHistory.getRelatedOrderItemIds()).hasSize(1);

        // 5. 외부 서비스 호출 검증
        verify(userServiceClient, times(1)).getUserByCode("USER-001");
        verify(productServiceClient, times(2)).getProductByCode("PROD-001"); // OrderValidator(1번) + createOrderWithItems(1번)
        verify(productServiceClient, times(1)).decreaseInventory("PROD-001", 2);
        
        // 5-1. Payment 파라미터 검증 (ArgumentCaptor 사용)
        ArgumentCaptor<OrderPaymentRequest> paymentRequestCaptor = ArgumentCaptor.forClass(OrderPaymentRequest.class);
        verify(paymentServiceClient, times(1)).requestDepositPayment(paymentRequestCaptor.capture());
        
        OrderPaymentRequest capturedPaymentRequest = paymentRequestCaptor.getValue();
        assertThat(capturedPaymentRequest.orderId()).isEqualTo(savedOrder.getId());
        assertThat(capturedPaymentRequest.orderCode()).isEqualTo(savedOrder.getCode());
        assertThat(capturedPaymentRequest.buyerId()).isEqualTo(1L);
        assertThat(capturedPaymentRequest.buyerCode()).isEqualTo("USER-001");
        assertThat(capturedPaymentRequest.paidAmount()).isEqualTo(20000);
        assertThat(capturedPaymentRequest.paidType()).isEqualTo(PaidType.DEPOSIT);
        assertThat(capturedPaymentRequest.paymentKey()).isNull();

        // 5-2. 이벤트 발행 검증 (주문 완료 이벤트)
        verify(orderEventService, times(1)).publishOrderCompletedEvents(
                eq(savedOrder),
                eq(orderItems),
                eq("USER-001")
        );

        log.info("*****************************after assertions*****************************");
    }

    /**
     * 통합 테스트: 다이렉트 주문 생성 - 토스 결제 (TOSS_PAYMENT)
     * <p>
     * 검증 항목:
     * 1. 실제 DB에 주문이 저장되는지 확인
     * 2. 실제 OrderValidator가 재고 검증을 수행하는지 확인
     * 3. 실제 트랜잭션이 동작하는지 확인
     * 4. 외부 서비스 호출이 올바른지 확인
     * 5. 토스 결제는 주문만 생성하고 결제는 하지 않음 (상태: CREATED)
     * 6. 결제 API가 호출되지 않음을 확인
     * 7. 주문 완료 이벤트가 발행되지 않음을 확인
     */
    @DisplayName("통합 테스트: 다이렉트 주문 생성 - 토스 결제 (TOSS_PAYMENT)")
    @Test
    @Transactional
    void createDirectOrder_TossPayment() {
        // given
        log.info("*****************************before createDirectOrder (TOSS_PAYMENT)*****************************");
        String userCode = "USER-001";
        OrderDirectRequest request = new OrderDirectRequest(
                "PROD-001",
                2,
                "서울시 강남구",
                OrderType.ONLINE,
                PaidType.TOSS_PAYMENT,
                null
        );

        // 외부 서비스 Mock 설정 (다른 마이크로서비스)
        when(userServiceClient.getUserByCode("USER-001")).thenReturn(testUser);
        when(productServiceClient.getProductByCode("PROD-001")).thenReturn(testProduct);
        doNothing().when(productServiceClient).decreaseInventory(anyString(), any());
        // TOSS_PAYMENT는 결제 API를 호출하지 않으므로 Mock 설정 불필요

        // when - 실제 OrderService 호출 (실제 DB 사용)
        OrderCreateResponse result = orderService.createDirectOrder(request, userCode);
        log.info("*****************************after createDirectOrder (TOSS_PAYMENT)*****************************");
        
        // then - 통합 테스트 검증
        // 1. 응답 검증
        log.info("*****************************응답 검증 (TOSS_PAYMENT)*****************************");
        assertThat(result).isNotNull();
        assertThat(result.orderCode()).isNotNull();
        assertThat(result.buyerId()).isEqualTo(1L);
        assertThat(result.totalPrice()).isEqualTo(20000); // 2개 * 10000원
        assertThat(result.orderStatus()).isEqualTo(OrderStatus.CREATED); // TOSS_PAYMENT는 CREATED 상태로 유지
        assertThat(result.orderType()).isEqualTo(OrderType.ONLINE);
        assertThat(result.address()).isEqualTo("서울시 강남구");
        assertThat(result.orderItems()).hasSize(1);

        // 2. 실제 DB에 저장되었는지 확인 (통합 테스트의 핵심!)
        Order savedOrder = orderJpaRepository.findByCode(result.orderCode());
        log.info("*****************************savedOrder (TOSS_PAYMENT)*****************************");
        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatus.CREATED); // TOSS_PAYMENT는 CREATED 상태로 유지
        assertThat(savedOrder.getTotalPrice()).isEqualTo(20000);
        assertThat(savedOrder.getBuyerId()).isEqualTo(1L);
        assertThat(savedOrder.getBuyerCode()).isEqualTo("USER-001");

        // 3. 실제 DB에 주문 항목이 저장되었는지 확인
        List<OrderItem> orderItems = orderItemJpaRepository.findByOrderId(savedOrder.getId());
        log.info("*****************************orderItems (TOSS_PAYMENT)*****************************");
        assertThat(orderItems).hasSize(1);
        assertThat(orderItems.getFirst().getQuantity()).isEqualTo(2);
        assertThat(orderItems.getFirst().getPrice()).isEqualTo(10000);
        assertThat(orderItems.getFirst().getProductCode()).isEqualTo("PROD-001");
        assertThat(orderItems.getFirst().getProductId()).isEqualTo(1L);

        // 4. 실제 DB에 주문 이력이 저장되었는지 확인
        // 주문 생성 시: CREATE 이력만 저장 (결제는 하지 않으므로 STATUS_CHANGE 이력 없음)
        List<OrderHistoryEntity> orderHistories = orderHistoryJpaRepository.findByOrderId(savedOrder.getId());
        log.info("*****************************orderHistories (TOSS_PAYMENT)*****************************");
        assertThat(orderHistories).hasSizeGreaterThanOrEqualTo(1); // 최소 1개 이상 (생성만)
        
        // 주문 생성 이력 확인
        OrderHistoryEntity createHistory = orderHistories.stream()
                .filter(history -> history.getActionType() == OrderHistoryActionType.CREATE)
                .findFirst()
                .orElse(null);
        log.info("*****************************createHistory (TOSS_PAYMENT)*****************************");
        assertThat(createHistory).isNotNull();
        assertThat(createHistory.getOrderCode()).isEqualTo(savedOrder.getCode());
        assertThat(createHistory.getCurrentStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(createHistory.getPreviousStatus()).isNull(); // 처음 생성이므로 이전 상태 없음
        assertThat(createHistory.getRelatedOrderItemIds()).hasSize(1);
        assertThat(createHistory.getRelatedOrderItemIds().get(0)).isEqualTo(orderItems.get(0).getId());
        
        // 결제 성공 이력이 없어야 함 (TOSS_PAYMENT는 결제를 하지 않음)
        OrderHistoryEntity paymentSuccessHistory = orderHistories.stream()
                .filter(history -> history.getActionType() == OrderHistoryActionType.STATUS_CHANGE)
                .filter(history -> history.getCurrentStatus() == OrderStatus.COMPLETED)
                .findFirst()
                .orElse(null);
        assertThat(paymentSuccessHistory).isNull(); // TOSS_PAYMENT는 결제를 하지 않으므로 없어야 함

        // 5. 외부 서비스 호출 검증
        verify(userServiceClient, times(1)).getUserByCode("USER-001");
        verify(productServiceClient, times(2)).getProductByCode("PROD-001"); // OrderValidator(1번) + createOrderWithItems(1번)
        verify(productServiceClient, times(1)).decreaseInventory("PROD-001", 2);
        
        // 5-1. TOSS_PAYMENT는 결제 API를 호출하지 않음
        verify(paymentServiceClient, never()).requestDepositPayment(any());
        verify(paymentServiceClient, never()).requestTossPayment(any());

        // 5-2. TOSS_PAYMENT는 주문 완료 이벤트를 발행하지 않음 (CREATED 상태이므로)
        verify(orderEventService, never()).publishOrderCompletedEvents(any(), any(), anyString());

        log.info("*****************************after assertions (TOSS_PAYMENT)*****************************");
    }

}

