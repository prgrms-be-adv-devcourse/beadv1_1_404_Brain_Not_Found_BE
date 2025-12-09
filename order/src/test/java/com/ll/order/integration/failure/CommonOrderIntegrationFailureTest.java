package com.ll.order.integration.failure;

import com.ll.core.model.exception.BaseException;
import com.ll.order.domain.exception.OrderErrorCode;
import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.entity.TransactionTracing;
import com.ll.order.domain.model.entity.history.OrderHistoryEntity;
import com.ll.order.domain.model.enums.order.OrderHistoryActionType;
import com.ll.order.domain.model.enums.order.OrderStatus;
import com.ll.order.domain.model.enums.order.OrderType;
import com.ll.order.domain.model.enums.payment.PaidType;
import com.ll.order.domain.model.enums.transaction.CompensationStatus;
import com.ll.order.domain.model.vo.request.OrderCartItemRequest;
import com.ll.order.domain.model.vo.request.OrderDirectRequest;
import com.ll.order.domain.model.vo.request.OrderPaymentRequest;
import com.ll.order.domain.model.vo.response.cart.CartItemsResponse;
import com.ll.order.domain.model.vo.response.order.OrderCreateResponse;
import com.ll.order.domain.model.vo.response.product.ProductResponse;
import com.ll.order.domain.model.vo.response.user.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("공통 주문 실패 통합 테스트")
@Slf4j
class CommonOrderIntegrationFailureTest extends BaseOrderIntegrationFailureTest {

    // 실제 CompensationService를 사용하기 위해 Mock 설정을 변경
    // Mock의 markCompensationFailed 메서드가 실제 메서드를 호출하도록 설정

    private UserResponse testUser;
    private ProductResponse testProduct1;
    private ProductResponse testProduct2;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        testProduct1 = createTestProduct("PROD-001", 10, 10000);
        testProduct2 = createTestProduct("PROD-002", 5, 15000);
    }
    // ========== 1. 재고 차감 실패 시 보상 로직 ==========
    
    @DisplayName("보상 로직: 다이렉트 주문 - 단일 상품 재고 차감 실패 (롤백 대상 없음)")
    @Test
    @Transactional
    void createDirectOrder_InventoryDeductionFailure_NoRollback() {
        // given
        String userCode = "USER-001";
        OrderDirectRequest request = new OrderDirectRequest(
                "PROD-001",
                2,
                "서울시 강남구",
                OrderType.ONLINE,
                PaidType.DEPOSIT,
                null
        );

        // 외부 서비스 Mock 설정
        when(userServiceClient.getUserByCode("USER-001")).thenReturn(testUser);
        when(productServiceClient.getProductByCode("PROD-001")).thenReturn(testProduct1);
        
        // 재고 차감 실패 Mock 설정
        doThrow(new RuntimeException("재고 차감 API 실패"))
                .when(productServiceClient).decreaseInventory(anyString(), anyInt());

        // when & then - 예외 발생 검증
        assertThatThrownBy(() -> orderService.createDirectOrder(request, userCode))
                .isInstanceOf(BaseException.class) // 발생한 예외가 BaseException 타입인지 확인
                .satisfies(exception -> {
                    BaseException baseException = (BaseException) exception;
                    assertThat(baseException.getErrorCode()).isEqualTo(OrderErrorCode.INVENTORY_DEDUCTION_FAILED);
                });

        // 롤백 호출 없음 (성공한 재고 차감이 없으므로)
        verify(orderEventProducer, never()).sendInventoryRollback(anyString(), anyInt());

        // 재고 차감 호출 확인 (주문 생성 완료 후 재고 차감 단계까지 도달했음을 의미)
        verify(productServiceClient, times(1)).decreaseInventory("PROD-001", 2);

        // 결제 처리 호출 없음 (재고 차감 실패로 인해 결제 단계까지 도달하지 않음)
        verify(paymentServiceClient, never()).requestDepositPayment(any(OrderPaymentRequest.class));

        // 주문 완료 이벤트 발행 없음 (재고 차감 실패로 인해 주문이 완료되지 않음)
        verify(orderEventService, never()).publishOrderCompletedEvents(any(), any(), anyString());

        // 주문이 생성되고 FAILED 상태로 변경되었는지 확인
        List<Order> orders = orderJpaRepository.findAll();
        assertThat(orders).hasSize(1);
        Order savedOrder = orders.getFirst();
        assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatus.FAILED);

        // OrderItem이 저장되었는지 확인
        long orderItemCount = orderItemJpaRepository.count();
        assertThat(orderItemCount).isEqualTo(1);

        // OrderHistory가 저장되었는지 확인 (재고 차감 실패 이력)
        List<OrderHistoryEntity> orderHistories = orderHistoryJpaRepository.findByOrderId(savedOrder.getId());
        assertThat(orderHistories).isNotEmpty();
        
        // 재고 차감 실패 이력이 저장되었는지 확인
        OrderHistoryEntity inventoryFailureHistory = orderHistories.stream()
                .filter(history -> "재고 차감 실패".equals(history.getReason()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("재고 차감 실패 이력이 저장되지 않았습니다."));
        
        assertThat(inventoryFailureHistory.getActionType()).isEqualTo(OrderHistoryActionType.STATUS_CHANGE);
        assertThat(inventoryFailureHistory.getCurrentStatus()).isEqualTo(OrderStatus.FAILED);
        assertThat(inventoryFailureHistory.getReason()).isEqualTo("재고 차감 실패");
        assertThat(inventoryFailureHistory.getErrorMessage()).isNotNull();
        assertThat(inventoryFailureHistory.getErrorMessage()).contains("PROD-001");

        // 트랜잭션 관리 검증: TransactionTracing이 생성되었지만 보상 로직은 실행되지 않음
        // (롤백할 성공한 재고 차감이 없으므로)
        // @Autowired로 실제 빈을 사용하므로 verify() 대신 assertThat()으로 실제 상태를 확인
        String orderCode = savedOrder.getCode();
        Optional<TransactionTracing> tracingOpt = transactionTracingRepository.findByOrderCode(orderCode);
        assertThat(tracingOpt).isPresent();
        TransactionTracing tracing = tracingOpt.get();
        // TransactionTracing은 생성되었지만, 보상 로직이 호출되지 않았으므로 상태는 NONE
        assertThat(tracing.getCompensationStatus()).isEqualTo(CompensationStatus.NONE);
        assertThat(tracing.getCompensationRetryCount()).isEqualTo(0);
    }

    @DisplayName("보상 로직: 카트 주문 - 여러 상품 중 일부 재고 차감 실패 (부분 롤백)")
    @Test
    @Transactional
    void createCartOrder_InventoryDeductionFailure_PartialRollback() {
        // given
        String userCode = "USER-001";
        OrderCartItemRequest request = createCartOrderRequest();
        CartItemsResponse cartResponse = createCartResponse();

        // 외부 서비스 Mock 설정
        when(userServiceClient.getUserByCode("USER-001")).thenReturn(testUser);
        when(cartServiceClient.getCartByCode("USER-001")).thenReturn(cartResponse); // getCartByCode는 userCode를 받음
        when(productServiceClient.getProductByCode("PROD-001")).thenReturn(testProduct1);
        when(productServiceClient.getProductByCode("PROD-002")).thenReturn(testProduct2);
        
        // 재고 차감 Mock 설정: 첫 번째 성공, 두 번째 실패
        doNothing()
                .when(productServiceClient).decreaseInventory("PROD-001", 2);
        doThrow(new RuntimeException("재고 차감 API 실패"))
                .when(productServiceClient).decreaseInventory("PROD-002", 1);

        // when & then - 예외 발생 검증
        assertThatThrownBy(() -> orderService.createCartItemOrder(request, userCode))
                .isInstanceOf(BaseException.class)
                .satisfies(exception -> {
                    BaseException baseException = (BaseException) exception;
                    assertThat(baseException.getErrorCode()).isEqualTo(OrderErrorCode.INVENTORY_DEDUCTION_FAILED);
                });

        // 성공한 재고 차감만 롤백 이벤트 발행
        ArgumentCaptor<String> productCodeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> quantityCaptor = ArgumentCaptor.forClass(Integer.class);
        
        verify(orderEventProducer, times(1)).sendInventoryRollback(
                productCodeCaptor.capture(),
                quantityCaptor.capture()
        );
        assertThat(productCodeCaptor.getValue()).isEqualTo("PROD-001");
        assertThat(quantityCaptor.getValue()).isEqualTo(2);
        // 실패한 상품(PROD-002)은 롤백 이벤트 발행되지 않음
        verify(orderEventProducer, never()).sendInventoryRollback("PROD-002", 1);
        // 재고 차감 호출 확인
        verify(productServiceClient, times(1)).decreaseInventory("PROD-001", 2);
        verify(productServiceClient, times(1)).decreaseInventory("PROD-002", 1);
        // 결제 처리 호출 없음 (재고 차감 실패로 인해 결제 단계까지 도달하지 않음)
        verify(paymentServiceClient, never()).requestDepositPayment(any(OrderPaymentRequest.class));
        // 주문 완료 이벤트 발행 없음 (재고 차감 실패로 인해 주문이 완료되지 않음)
        verify(orderEventService, never()).publishOrderCompletedEvents(any(), any(), anyString());
        // 주문이 저장되었는지 확인 (롤백되지 않음 - 이력 추적을 위해)
        long orderCount = orderJpaRepository.count();
        assertThat(orderCount).isEqualTo(1);

        // 주문 상태가 FAILED로 변경되었는지 확인
        Order savedOrder = orderJpaRepository.findAll().getFirst();
        assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatus.FAILED);

        // OrderItem이 저장되었는지 확인
        long orderItemCount = orderItemJpaRepository.count();
        assertThat(orderItemCount).isEqualTo(2);

        // OrderHistory가 저장되었는지 확인 (재고 차감 실패 이력)
        List<OrderHistoryEntity> orderHistories = orderHistoryJpaRepository.findByOrderId(savedOrder.getId());
        assertThat(orderHistories).isNotEmpty();
        
        // 재고 차감 실패 이력이 저장되었는지 확인
        OrderHistoryEntity inventoryFailureHistory = orderHistories.stream()
                .filter(history -> "재고 차감 실패".equals(history.getReason()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("재고 차감 실패 이력이 저장되지 않았습니다."));
        
        assertThat(inventoryFailureHistory.getActionType()).isEqualTo(OrderHistoryActionType.STATUS_CHANGE);
        assertThat(inventoryFailureHistory.getCurrentStatus()).isEqualTo(OrderStatus.FAILED);
        assertThat(inventoryFailureHistory.getReason()).isEqualTo("재고 차감 실패");
        assertThat(inventoryFailureHistory.getErrorMessage()).isNotNull();
        assertThat(inventoryFailureHistory.getErrorMessage()).contains("PROD-002");

        // 트랜잭션 관리 검증: TransactionTracing이 생성되었지만 보상 로직은 실행되지 않음
        // (이벤트 발행이 성공했으므로 보상 상태 저장 불필요)
        String orderCode = savedOrder.getCode();
        Optional<TransactionTracing> tracingOpt = transactionTracingRepository.findByOrderCode(orderCode);
        assertThat(tracingOpt).isPresent();
        TransactionTracing tracing = tracingOpt.get();
        // TransactionTracing은 생성되었지만, 보상 로직이 호출되지 않았으므로 상태는 NONE
        assertThat(tracing.getCompensationStatus()).isEqualTo(CompensationStatus.NONE);
     }

    // ========== 2. 결제 실패 시 보상 로직 ==========
    
    @DisplayName("보상 로직: 다이렉트 주문 - 결제 실패 시 재고 롤백")
    @Test
    @Transactional
    void createDirectOrder_PaymentFailure_InventoryRollback() {
        // given
        String userCode = "USER-001";
        OrderDirectRequest request = new OrderDirectRequest(
                "PROD-001",
                2,
                "서울시 강남구",
                OrderType.ONLINE,
                PaidType.DEPOSIT,
                null
        );

        // 외부 서비스 Mock 설정
        when(userServiceClient.getUserByCode("USER-001")).thenReturn(testUser);
        when(productServiceClient.getProductByCode("PROD-001")).thenReturn(testProduct1);

        // 재고 차감 성공 Mock 설정
        doNothing().when(productServiceClient).decreaseInventory(anyString(), anyInt());

        // 결제 실패 Mock 설정
        doThrow(new RuntimeException("결제 처리 실패"))
                .when(paymentServiceClient).requestDepositPayment(any(OrderPaymentRequest.class));

        // when & then - 예외 발생 검증
        assertThatThrownBy(() -> orderService.createDirectOrder(request, userCode))
                .isInstanceOf(BaseException.class)
                .satisfies(exception -> {
                    BaseException baseException = (BaseException) exception;
                    assertThat(baseException.getErrorCode()).isEqualTo(OrderErrorCode.PAYMENT_PROCESSING_FAILED);
                });

        // 재고 롤백 호출 확인
        ArgumentCaptor<String> productCodeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> quantityCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(orderEventProducer, times(1)).sendInventoryRollback(
                productCodeCaptor.capture(),
                quantityCaptor.capture()
        );

        assertThat(productCodeCaptor.getValue()).isEqualTo("PROD-001");
        assertThat(quantityCaptor.getValue()).isEqualTo(2);

        // 재고 차감 호출 확인
        verify(productServiceClient, times(1)).decreaseInventory("PROD-001", 2);

        // 결제 처리 호출 확인
        verify(paymentServiceClient, times(1)).requestDepositPayment(any(OrderPaymentRequest.class));

        // 주문 완료 이벤트 발행 없음 (결제 실패로 인해 주문이 완료되지 않음)
        verify(orderEventService, never()).publishOrderCompletedEvents(any(), any(), anyString());

        // 주문이 생성되고 FAILED 상태로 변경되었는지 확인
        List<Order> orders = orderJpaRepository.findAll();
        assertThat(orders).hasSize(1);
        Order savedOrder = orders.getFirst();
        assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatus.FAILED);

        // OrderItem이 저장되었는지 확인
        long orderItemCount = orderItemJpaRepository.count();
        assertThat(orderItemCount).isEqualTo(1);

        // OrderHistory가 저장되었는지 확인 (결제 실패 이력)
        List<OrderHistoryEntity> orderHistories = orderHistoryJpaRepository.findByOrderId(savedOrder.getId());
        assertThat(orderHistories).isNotEmpty();
        
        // 결제 실패 이력이 저장되었는지 확인
        OrderHistoryEntity paymentFailureHistory = orderHistories.stream()
                .filter(history -> "예치금 결제 실패".equals(history.getReason()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("결제 실패 이력이 저장되지 않았습니다."));
        
        assertThat(paymentFailureHistory.getActionType()).isEqualTo(OrderHistoryActionType.STATUS_CHANGE);
        assertThat(paymentFailureHistory.getCurrentStatus()).isEqualTo(OrderStatus.FAILED);
        assertThat(paymentFailureHistory.getReason()).isEqualTo("예치금 결제 실패");
        assertThat(paymentFailureHistory.getErrorMessage()).isNotNull();
    }

    @DisplayName("보상 로직: 카트 주문 - 결제 실패 시 재고 롤백")
    @Test
    @Transactional
    void createCartOrder_PaymentFailure_InventoryRollback() {
        // given
        String userCode = "USER-001";
        OrderCartItemRequest request = createCartOrderRequest();
        CartItemsResponse cartResponse = createCartResponse();

        // 외부 서비스 Mock 설정
        when(userServiceClient.getUserByCode("USER-001")).thenReturn(testUser);
        when(cartServiceClient.getCartByCode("USER-001")).thenReturn(cartResponse);
        when(productServiceClient.getProductByCode("PROD-001")).thenReturn(testProduct1);
        when(productServiceClient.getProductByCode("PROD-002")).thenReturn(testProduct2);

        // 재고 차감 성공 Mock 설정
        doNothing().when(productServiceClient).decreaseInventory(anyString(), anyInt());

        // 결제 실패 Mock 설정
        doThrow(new RuntimeException("결제 처리 실패"))
                .when(paymentServiceClient).requestDepositPayment(any(OrderPaymentRequest.class));

        // when & then - 예외 발생 검증
        assertThatThrownBy(() -> orderService.createCartItemOrder(request, userCode))
                .isInstanceOf(BaseException.class)
                .satisfies(exception -> {
                    BaseException baseException = (BaseException) exception;
                    assertThat(baseException.getErrorCode()).isEqualTo(OrderErrorCode.PAYMENT_PROCESSING_FAILED);
                });

        // 재고 롤백 호출 확인 (모든 상품에 대해)
        verify(orderEventProducer, times(2)).sendInventoryRollback(anyString(), anyInt());

        // 재고 차감 호출 확인
        verify(productServiceClient, times(1)).decreaseInventory("PROD-001", 2);
        verify(productServiceClient, times(1)).decreaseInventory("PROD-002", 1);

        // 결제 처리 호출 확인
        verify(paymentServiceClient, times(1)).requestDepositPayment(any(OrderPaymentRequest.class));

        // 주문 완료 이벤트 발행 없음 (결제 실패로 인해 주문이 완료되지 않음)
        verify(orderEventService, never()).publishOrderCompletedEvents(any(), any(), anyString());

        // 주문이 생성되고 FAILED 상태로 변경되었는지 확인
        List<Order> orders = orderJpaRepository.findAll();
        assertThat(orders).hasSize(1);
        Order savedOrder = orders.getFirst();
        assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatus.FAILED);

        // OrderItem이 저장되었는지 확인
        long orderItemCount = orderItemJpaRepository.count();
        assertThat(orderItemCount).isEqualTo(2);

        // OrderHistory가 저장되었는지 확인 (결제 실패 이력)
        List<OrderHistoryEntity> orderHistories = orderHistoryJpaRepository.findByOrderId(savedOrder.getId());
        assertThat(orderHistories).isNotEmpty();
        
        // 결제 실패 이력이 저장되었는지 확인
        OrderHistoryEntity paymentFailureHistory = orderHistories.stream()
                .filter(history -> "예치금 결제 실패".equals(history.getReason()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("결제 실패 이력이 저장되지 않았습니다."));
        
        assertThat(paymentFailureHistory.getActionType()).isEqualTo(OrderHistoryActionType.STATUS_CHANGE);
        assertThat(paymentFailureHistory.getCurrentStatus()).isEqualTo(OrderStatus.FAILED);
        assertThat(paymentFailureHistory.getReason()).isEqualTo("예치금 결제 실패");
        assertThat(paymentFailureHistory.getErrorMessage()).isNotNull();

        // 트랜잭션 관리 검증: TransactionTracing이 생성되었지만 보상 로직은 실행되지 않음
        // (이벤트 발행이 성공했으므로 보상 상태 저장 불필요)
        String orderCode = savedOrder.getCode();
        Optional<TransactionTracing> tracingOpt = transactionTracingRepository.findByOrderCode(orderCode);
        assertThat(tracingOpt).isPresent();
        TransactionTracing tracing = tracingOpt.get();
        // TransactionTracing은 생성되었지만, 보상 로직이 호출되지 않았으므로 상태는 NONE
        assertThat(tracing.getCompensationStatus()).isEqualTo(CompensationStatus.NONE);
    }

    // ========== 3. 보상 로직 실패 시나리오 ==========
    
    @DisplayName("보상 로직: 재고 롤백 이벤트 발행 실패 시 REQUIRES_NEW로 보상 상태 저장")
    @Test
    @Transactional
    void testCompensationFailed_WhenRollbackEventPublishFails() {
        // given
        String userCode = "USER-001";
        OrderDirectRequest request = new OrderDirectRequest(
                "PROD-001",
                2,
                "서울시 강남구",
                OrderType.ONLINE,
                PaidType.DEPOSIT,
                null
        );

        // 외부 서비스 Mock 설정
        when(userServiceClient.getUserByCode("USER-001")).thenReturn(testUser);
        when(productServiceClient.getProductByCode("PROD-001")).thenReturn(testProduct1);

        // 재고 차감 성공 Mock 설정
        doNothing().when(productServiceClient).decreaseInventory(anyString(), anyInt());

        // 결제 실패 Mock 설정
        doThrow(new RuntimeException("결제 처리 실패"))
                .when(paymentServiceClient).requestDepositPayment(any(OrderPaymentRequest.class));

        // 이벤트 발행 실패 Mock 설정 (보상 로직 실패 시나리오)
        doThrow(new RuntimeException("Kafka 연결 실패"))
                .when(orderEventProducer).sendInventoryRollback(anyString(), anyInt());

        // when & then - 예외 발생 검증
        assertThatThrownBy(() -> orderService.createDirectOrder(request, userCode))
                .isInstanceOf(BaseException.class)
                .satisfies(exception -> {
                    BaseException baseException = (BaseException) exception;
                    assertThat(baseException.getErrorCode()).isEqualTo(OrderErrorCode.PAYMENT_PROCESSING_FAILED);
                });

        // 주문이 생성되었는지 확인
        Order savedOrder = orderJpaRepository.findAll().getFirst();
        String orderCode = savedOrder.getCode();

        // TransactionTracing이 주문 생성 시점에 생성되었는지 확인
        // 메인 트랜잭션 내에서 조회 (createOrderWithItems의 트랜잭션이 커밋되었으므로 조회 가능)
        Optional<TransactionTracing> tracingOpt = transactionTracingRepository.findByOrderCode(orderCode);
        assertThat(tracingOpt).isPresent();
        TransactionTracing beforeTracing = tracingOpt.get();
        assertThat(beforeTracing).isNotNull();
        
        // 호출 전 상태 확인
        log.info("=== 호출 전 상태 ===");
        log.info("compensationStatus: {}", beforeTracing.getCompensationStatus());
        log.info("compensationRetryCount: {}", beforeTracing.getCompensationRetryCount());
        log.info("errorMessage: {}", beforeTracing.getErrorMessage());
        
        // 실제 CompensationService가 동작하여 상태가 변경되었는지 검증
        // doCallRealMethod()로 설정했으므로 서버 코드에서 호출된 Mock이 실제 메서드를 실행했을 것임
        // 서버 코드에서 이미 실행되었는지 확인

        // 트랜잭션 관리 검증: REQUIRES_NEW로 저장된 보상 상태 확인
        // 별도 트랜잭션에서 다시 조회하여 REQUIRES_NEW 동작 검증 (커밋 여부 확인)
        log.info("=== 별도 트랜잭션에서 조회 시작 (커밋 여부 확인) ===");
        TransactionTracing updatedTracing = transactionTemplate.execute(status -> {
            Optional<TransactionTracing> updatedTracingOpt = transactionTracingRepository.findByOrderCode(orderCode);
            if (updatedTracingOpt.isPresent()) {
                TransactionTracing tracing = updatedTracingOpt.get();
                log.info("별도 트랜잭션에서 조회 성공 - compensationStatus: {}, retryCount: {}, errorMessage: {}",
                        tracing.getCompensationStatus(), tracing.getCompensationRetryCount(), tracing.getErrorMessage());
                return tracing;
            } else {
                log.warn("별도 트랜잭션에서 TransactionTracing을 찾을 수 없습니다!");
                return null;
            }
        });
        log.info("=== 별도 트랜잭션에서 조회 완료 ===");

        // 실제 CompensationService가 동작하여 상태가 변경되었는지 검증
        assertThat(updatedTracing).isNotNull();
        assertThat(updatedTracing.getCompensationStatus()).isEqualTo(CompensationStatus.FAILED);
        assertThat(updatedTracing.getCompensationRetryCount()).isEqualTo(1);
        assertThat(updatedTracing.getErrorMessage()).isNotNull();
        assertThat(updatedTracing.getErrorMessage()).contains("Kafka 연결 실패");
        // REQUIRES_NEW 트랜잭션으로 인해 메인 트랜잭션이 롤백되어도 보상 상태는 유지됨을 확인
        // 별도 트랜잭션에서 조회했으므로 실제로 저장되었음을 검증
    }
 
    // ========== 4. 토스 결제 실패 시 보상 로직 ==========

    @DisplayName("보상 로직: 토스 결제 - 결제 완료 처리 실패 시 재고 롤백")
    @Test
    @Transactional
    void completeTossPayment_PaymentFailure_InventoryRollback() {
        // given
        String userCode = "USER-001";
        String paymentKey = "toss_payment_key_12345";
        OrderDirectRequest request = new OrderDirectRequest(
                "PROD-001",
                2,
                "서울시 강남구",
                OrderType.ONLINE,
                PaidType.TOSS_PAYMENT,
                null
        );

        // 외부 서비스 Mock 설정
        when(userServiceClient.getUserByCode("USER-001")).thenReturn(testUser);
        when(productServiceClient.getProductByCode("PROD-001")).thenReturn(testProduct1);

        // 재고 차감 성공 Mock 설정
        doNothing().when(productServiceClient).decreaseInventory(anyString(), anyInt());

        // 토스 결제 주문 생성 (재고 차감 성공, 주문 CREATED 상태)
        OrderCreateResponse orderResponse = orderService.createDirectOrder(request, userCode);
        String orderCode = orderResponse.orderCode();

        // 주문이 CREATED 상태로 생성되었는지 확인
        Order createdOrder = Optional.ofNullable(orderJpaRepository.findByCode(orderCode))
                .orElseThrow(() -> new AssertionError("주문이 생성되지 않았습니다."));
        assertThat(createdOrder.getOrderStatus()).isEqualTo(OrderStatus.CREATED);

        // 결제 완료 처리 실패 Mock 설정
        doThrow(new RuntimeException("토스 결제 처리 실패"))
                .when(paymentServiceClient).requestTossPayment(any(OrderPaymentRequest.class));

        // when & then - 예외 발생 검증
        assertThatThrownBy(() -> orderService.completePaymentWithKey(orderCode, paymentKey))
                .isInstanceOf(BaseException.class)
                .satisfies(exception -> {
                    BaseException baseException = (BaseException) exception;
                    assertThat(baseException.getErrorCode()).isEqualTo(OrderErrorCode.PAYMENT_PROCESSING_FAILED);
                });

        // 재고 롤백 호출 확인
        ArgumentCaptor<String> productCodeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> quantityCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(orderEventProducer, times(1)).sendInventoryRollback(
                productCodeCaptor.capture(),
                quantityCaptor.capture()
        );

        assertThat(productCodeCaptor.getValue()).isEqualTo("PROD-001");
        assertThat(quantityCaptor.getValue()).isEqualTo(2);

        // 결제 완료 처리 호출 확인
        verify(paymentServiceClient, times(1)).requestTossPayment(any(OrderPaymentRequest.class));

        // 주문 완료 이벤트 발행 없음 (결제 실패로 인해 주문이 완료되지 않음)
        verify(orderEventService, never()).publishOrderCompletedEvents(any(), any(), anyString());

        // 주문 상태가 FAILED로 변경되었는지 확인
        Order failedOrder = Optional.ofNullable(orderJpaRepository.findByCode(orderCode))
                .orElseThrow(() -> new AssertionError("주문을 찾을 수 없습니다."));
        assertThat(failedOrder.getOrderStatus()).isEqualTo(OrderStatus.FAILED);

        // OrderHistory가 저장되었는지 확인 (결제 실패 이력)
        List<OrderHistoryEntity> orderHistories = orderHistoryJpaRepository.findByOrderId(failedOrder.getId());
        assertThat(orderHistories).isNotEmpty();
        
        // 결제 실패 이력이 저장되었는지 확인
        OrderHistoryEntity paymentFailureHistory = orderHistories.stream()
                .filter(history -> "토스 결제 실패".equals(history.getReason()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("결제 실패 이력이 저장되지 않았습니다."));
        
        assertThat(paymentFailureHistory.getActionType()).isEqualTo(OrderHistoryActionType.STATUS_CHANGE);
        assertThat(paymentFailureHistory.getCurrentStatus()).isEqualTo(OrderStatus.FAILED);
        assertThat(paymentFailureHistory.getReason()).isEqualTo("토스 결제 실패");
        assertThat(paymentFailureHistory.getErrorMessage()).isNotNull();
    }

}

