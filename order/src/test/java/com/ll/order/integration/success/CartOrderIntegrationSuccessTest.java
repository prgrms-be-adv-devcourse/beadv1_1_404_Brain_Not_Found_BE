package com.ll.order.integration.success;

import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.entity.OrderItem;
import com.ll.order.domain.model.entity.history.OrderHistoryEntity;
import com.ll.order.domain.model.enums.order.OrderHistoryActionType;
import com.ll.order.domain.model.enums.order.OrderStatus;
import com.ll.order.domain.model.enums.order.OrderType;
import com.ll.order.domain.model.enums.payment.PaidType;
import com.ll.order.domain.model.vo.request.OrderCartItemRequest;
import com.ll.order.domain.model.vo.request.OrderPaymentRequest;
import com.ll.order.domain.model.vo.request.ProductRequest;
import com.ll.order.domain.model.vo.response.cart.CartItemInfo;
import com.ll.order.domain.model.vo.response.cart.CartItemsResponse;
import com.ll.order.domain.model.vo.response.order.OrderCreateResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("장바구니 주문-결제-재고 통합 테스트")
@Slf4j
class CartOrderIntegrationSuccessTest extends BaseOrderIntegrationSuccessTest {

    @DisplayName("통합 테스트: 장바구니 주문 생성 - 예치금 결제 (DEPOSIT)")
    @Test
    @Transactional
    void createCartOrder_DepositPayment() {
        // given
        log.info("*****************************before createCartOrder*****************************");
        String userCode = "USER-001";
        
        // 장바구니 주문 요청 생성 (2개 상품)
        List<ProductRequest> products = new ArrayList<>();
        products.add(new ProductRequest("PROD-001", 2, 10000, null));
        products.add(new ProductRequest("PROD-002", 1, 15000, null));
        
        OrderCartItemRequest request = new OrderCartItemRequest(
                "CART-001",
                "홍길동",
                "서울시 강남구",
                products,
                35000, // 2 * 10000 + 1 * 15000
                OrderType.ONLINE,
                PaidType.DEPOSIT,
                null
        );

        // 외부 서비스 Mock 설정 (다른 마이크로서비스)
        when(userServiceClient.getUserByCode("USER-001")).thenReturn(testUser);
        when(productServiceClient.getProductByCode("PROD-001")).thenReturn(testProduct);
        when(productServiceClient.getProductByCode("PROD-002")).thenReturn(testProduct2);
        
        // 장바구니 응답 Mock 설정
        List<CartItemInfo> cartItems = new ArrayList<>();
        cartItems.add(new CartItemInfo("CART-ITEM-001", 1L, "PROD-001", 2, 20000));
        cartItems.add(new CartItemInfo("CART-ITEM-002", 2L, "PROD-002", 1, 15000));
        CartItemsResponse cartResponse = new CartItemsResponse("CART-001", 35000, cartItems);
        when(cartServiceClient.getCartByCode("USER-001")).thenReturn(cartResponse);
        
        // 재고 추적 변수 (각 상품별로 추적)
        Map<String, AtomicInteger> stockMap = new ConcurrentHashMap<>();
        stockMap.put("PROD-001", new AtomicInteger(testProduct.quantity()));
        stockMap.put("PROD-002", new AtomicInteger(testProduct2.quantity()));
        
        // 재고 차감 Mock 설정: 실제 재고 차감을 시뮬레이션
        doAnswer(invocation -> {
            String productCode = invocation.getArgument(0);
            Integer quantity = invocation.getArgument(1);
            AtomicInteger currentStock = stockMap.get(productCode);
            if (currentStock != null) {
                int newStock = currentStock.addAndGet(-quantity);
                log.debug("재고 차감 완료 - productCode: {}, 차감량: {}, 남은 재고: {}", productCode, quantity, newStock);
            }
            return null;
        }).when(productServiceClient).decreaseInventory(anyString(), any());
        
        when(paymentServiceClient.requestDepositPayment(any())).thenReturn("OK");

        // when - 실제 OrderService 호출 (실제 DB 사용)
        OrderCreateResponse result = orderService.createCartItemOrder(request, userCode);
        log.info("*****************************after createCartOrder*****************************");
        
        // then - 통합 테스트 검증
        // 1. 응답 검증
        log.info("*****************************응답 검증*****************************");
        assertThat(result).isNotNull();
        assertThat(result.orderCode()).isNotNull();
        assertThat(result.buyerId()).isEqualTo(1L);
        assertThat(result.totalPrice()).isEqualTo(35000); // 2 * 10000 + 1 * 15000
        assertThat(result.orderStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(result.orderType()).isEqualTo(OrderType.ONLINE);
        assertThat(result.address()).isEqualTo("서울시 강남구");
        assertThat(result.orderItems()).hasSize(2); // 2개 상품

        // 2. 실제 DB에 저장되었는지 확인 (통합 테스트의 핵심!)
        Order savedOrder = orderJpaRepository.findByCode(result.orderCode());
        log.info("*****************************savedOrder*****************************");
        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(savedOrder.getTotalPrice()).isEqualTo(35000);
        assertThat(savedOrder.getBuyerId()).isEqualTo(1L);
        assertThat(savedOrder.getBuyerCode()).isEqualTo("USER-001");

        // 3. 실제 DB에 주문 항목이 저장되었는지 확인 (2개 상품)
        List<OrderItem> orderItems = orderItemJpaRepository.findByOrderId(savedOrder.getId());
        log.info("*****************************orderItems*****************************");
        assertThat(orderItems).hasSize(2);
        
        // 첫 번째 상품 검증
        OrderItem item1 = orderItems.stream()
                .filter(item -> item.getProductCode().equals("PROD-001"))
                .findFirst()
                .orElse(null);
        assertThat(item1).isNotNull();
        assertThat(item1.getQuantity()).isEqualTo(2);
        assertThat(item1.getPrice()).isEqualTo(10000);
        assertThat(item1.getProductId()).isEqualTo(1L);
        
        // 두 번째 상품 검증
        OrderItem item2 = orderItems.stream()
                .filter(item -> item.getProductCode().equals("PROD-002"))
                .findFirst()
                .orElse(null);
        assertThat(item2).isNotNull();
        assertThat(item2.getQuantity()).isEqualTo(1);
        assertThat(item2.getPrice()).isEqualTo(15000);
        assertThat(item2.getProductId()).isEqualTo(2L);

        // 4. 실제 DB에 주문 이력이 저장되었는지 확인
        // 주문 생성 시: CREATE 이력 저장
        // 결제 성공 시: STATUS_CHANGE 이력 저장 (COMPLETED 상태로 변경)
        List<OrderHistoryEntity> orderHistories = orderHistoryJpaRepository.findByOrderCode(savedOrder.getCode());
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
        assertThat(createHistory.getRelatedOrderItemIds()).hasSize(2); // 2개 상품
        
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
        assertThat(paymentSuccessHistory.getRelatedOrderItemIds()).hasSize(2); // 2개 상품

        // 5. 외부 서비스 호출 검증
        verify(userServiceClient, times(1)).getUserByCode("USER-001");
        verify(cartServiceClient, times(1)).getCartByCode("USER-001");
        verify(productServiceClient, times(2)).getProductByCode("PROD-001"); // OrderValidator(1번) + createOrderWithItems(1번)
        verify(productServiceClient, times(2)).getProductByCode("PROD-002"); // OrderValidator(1번) + createOrderWithItems(1번)
        
        // 5-0. 재고 차감 검증 (각 상품별로 1번씩 호출되어야 함)
        ArgumentCaptor<String> productCodeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> quantityCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(productServiceClient, times(2)).decreaseInventory(productCodeCaptor.capture(), quantityCaptor.capture());
        
        List<String> capturedProductCodes = productCodeCaptor.getAllValues();
        List<Integer> capturedQuantities = quantityCaptor.getAllValues();
        
        assertThat(capturedProductCodes).containsExactlyInAnyOrder("PROD-001", "PROD-002");
        assertThat(capturedQuantities).containsExactlyInAnyOrder(2, 1);
        
        // 재고 차감이 실제로 이루어졌는지 확인
        assertThat(stockMap.get("PROD-001").get()).isEqualTo(8); // 10 - 2 = 8
        assertThat(stockMap.get("PROD-002").get()).isEqualTo(9); // 10 - 1 = 9
        
        // 5-1. Payment 파라미터 검증 (ArgumentCaptor 사용)
        ArgumentCaptor<OrderPaymentRequest> paymentRequestCaptor = ArgumentCaptor.forClass(OrderPaymentRequest.class);
        verify(paymentServiceClient, times(1)).requestDepositPayment(paymentRequestCaptor.capture());
        
        OrderPaymentRequest capturedPaymentRequest = paymentRequestCaptor.getValue();
        assertThat(capturedPaymentRequest.orderId()).isEqualTo(savedOrder.getId());
        assertThat(capturedPaymentRequest.orderCode()).isEqualTo(savedOrder.getCode());
        assertThat(capturedPaymentRequest.buyerId()).isEqualTo(1L);
        assertThat(capturedPaymentRequest.buyerCode()).isEqualTo("USER-001");
        assertThat(capturedPaymentRequest.paidAmount()).isEqualTo(35000);
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

    @DisplayName("통합 테스트: 장바구니 주문 생성 - 토스 결제 (TOSS_PAYMENT)")
    @Test
    @Transactional
    void createCartOrder_TossPayment() {
        // given
        log.info("*****************************before createCartOrder (TOSS_PAYMENT)*****************************");
        String userCode = "USER-001";
        
        // 장바구니 주문 요청 생성 (2개 상품)
        List<ProductRequest> products = new ArrayList<>();
        products.add(new ProductRequest("PROD-001", 2, 10000, null));
        products.add(new ProductRequest("PROD-002", 1, 15000, null));
        
        OrderCartItemRequest request = new OrderCartItemRequest(
                "CART-001",
                "홍길동",
                "서울시 강남구",
                products,
                35000, // 2 * 10000 + 1 * 15000
                OrderType.ONLINE,
                PaidType.TOSS_PAYMENT,
                null
        );

        // 외부 서비스 Mock 설정 (다른 마이크로서비스)
        when(userServiceClient.getUserByCode("USER-001")).thenReturn(testUser);
        when(productServiceClient.getProductByCode("PROD-001")).thenReturn(testProduct);
        when(productServiceClient.getProductByCode("PROD-002")).thenReturn(testProduct2);
        
        // 장바구니 응답 Mock 설정
        List<CartItemInfo> cartItems = new ArrayList<>();
        cartItems.add(new CartItemInfo("CART-ITEM-001", 1L, "PROD-001", 2, 20000));
        cartItems.add(new CartItemInfo("CART-ITEM-002", 2L, "PROD-002", 1, 15000));
        CartItemsResponse cartResponse = new CartItemsResponse("CART-001", 35000, cartItems);
        when(cartServiceClient.getCartByCode("USER-001")).thenReturn(cartResponse);
        
        // 재고 추적 변수 (각 상품별로 추적)
        Map<String, AtomicInteger> stockMap = new ConcurrentHashMap<>();
        stockMap.put("PROD-001", new AtomicInteger(testProduct.quantity()));
        stockMap.put("PROD-002", new AtomicInteger(testProduct2.quantity()));
        
        // 재고 차감 Mock 설정: 실제 재고 차감을 시뮬레이션
        doAnswer(invocation -> {
            String productCode = invocation.getArgument(0);
            Integer quantity = invocation.getArgument(1);
            AtomicInteger currentStock = stockMap.get(productCode);
            if (currentStock != null) {
                int newStock = currentStock.addAndGet(-quantity);
                log.debug("재고 차감 완료 - productCode: {}, 차감량: {}, 남은 재고: {}", productCode, quantity, newStock);
            }
            return null;
        }).when(productServiceClient).decreaseInventory(anyString(), any());
        
        // when - 실제 OrderService 호출 (실제 DB 사용)
        OrderCreateResponse result = orderService.createCartItemOrder(request, userCode);
        log.info("*****************************after createCartOrder (TOSS_PAYMENT)*****************************");
        
        // then - 통합 테스트 검증
        // 1. 응답 검증
        log.info("*****************************응답 검증 (TOSS_PAYMENT)*****************************");
        assertThat(result).isNotNull();
        assertThat(result.orderCode()).isNotNull();
        assertThat(result.buyerId()).isEqualTo(1L);
        assertThat(result.totalPrice()).isEqualTo(35000); // 2 * 10000 + 1 * 15000
        assertThat(result.orderStatus()).isEqualTo(OrderStatus.CREATED); // TOSS_PAYMENT는 CREATED 상태로 유지
        assertThat(result.orderType()).isEqualTo(OrderType.ONLINE);
        assertThat(result.address()).isEqualTo("서울시 강남구");
        assertThat(result.orderItems()).hasSize(2); // 2개 상품

        // 2. 실제 DB에 저장되었는지 확인 (통합 테스트의 핵심!)
        Order savedOrder = orderJpaRepository.findByCode(result.orderCode());
        log.info("*****************************savedOrder (TOSS_PAYMENT)*****************************");
        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatus.CREATED); // TOSS_PAYMENT는 CREATED 상태로 유지
        assertThat(savedOrder.getTotalPrice()).isEqualTo(35000);
        assertThat(savedOrder.getBuyerId()).isEqualTo(1L);
        assertThat(savedOrder.getBuyerCode()).isEqualTo("USER-001");

        // 3. 실제 DB에 주문 상품이 저장되었는지 확인 (2개 상품)
        List<OrderItem> orderItems = orderItemJpaRepository.findByOrderId(savedOrder.getId());
        log.info("*****************************orderItems (TOSS_PAYMENT)*****************************");
        assertThat(orderItems).hasSize(2);
        
        // 첫 번째 상품 검증
        OrderItem item1 = orderItems.stream()
                .filter(item -> item.getProductCode().equals("PROD-001"))
                .findFirst()
                .orElse(null);
        assertThat(item1).isNotNull();
        assertThat(item1.getQuantity()).isEqualTo(2);
        assertThat(item1.getPrice()).isEqualTo(10000);
        assertThat(item1.getProductId()).isEqualTo(1L);
        
        // 두 번째 상품 검증
        OrderItem item2 = orderItems.stream()
                .filter(item -> item.getProductCode().equals("PROD-002"))
                .findFirst()
                .orElse(null);
        assertThat(item2).isNotNull();
        assertThat(item2.getQuantity()).isEqualTo(1);
        assertThat(item2.getPrice()).isEqualTo(15000);
        assertThat(item2.getProductId()).isEqualTo(2L);

        // 4. 실제 DB에 주문 이력이 저장되었는지 확인
        // 주문 생성 시: CREATE 이력만 저장 (결제는 하지 않으므로 STATUS_CHANGE 이력 없음)
        List<OrderHistoryEntity> orderHistories = orderHistoryJpaRepository.findByOrderCode(savedOrder.getCode());
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
        assertThat(createHistory.getRelatedOrderItemIds()).hasSize(2); // 2개 상품
        
        // 결제 성공 이력이 없어야 함 (TOSS_PAYMENT는 결제를 하지 않음)
        OrderHistoryEntity paymentSuccessHistory = orderHistories.stream()
                .filter(history -> history.getActionType() == OrderHistoryActionType.STATUS_CHANGE)
                .filter(history -> history.getCurrentStatus() == OrderStatus.COMPLETED)
                .findFirst()
                .orElse(null);
        assertThat(paymentSuccessHistory).isNull(); // TOSS_PAYMENT는 결제를 하지 않으므로 없어야 함

        // 5. 외부 서비스 호출 검증
        verify(userServiceClient, times(1)).getUserByCode("USER-001");
        verify(cartServiceClient, times(1)).getCartByCode("USER-001");
        verify(productServiceClient, times(2)).getProductByCode("PROD-001"); // OrderValidator(1번) + createOrderWithItems(1번)
        verify(productServiceClient, times(2)).getProductByCode("PROD-002"); // OrderValidator(1번) + createOrderWithItems(1번)
        
        // 5-0. 재고 차감 검증 (각 상품별로 1번씩 호출되어야 함)
        ArgumentCaptor<String> productCodeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> quantityCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(productServiceClient, times(2)).decreaseInventory(productCodeCaptor.capture(), quantityCaptor.capture());
        
        List<String> capturedProductCodes = productCodeCaptor.getAllValues();
        List<Integer> capturedQuantities = quantityCaptor.getAllValues();
        
        assertThat(capturedProductCodes).containsExactlyInAnyOrder("PROD-001", "PROD-002");
        assertThat(capturedQuantities).containsExactlyInAnyOrder(2, 1);
        
        // 재고 차감이 실제로 이루어졌는지 확인
        assertThat(stockMap.get("PROD-001").get()).isEqualTo(8); // 10 - 2 = 8
        assertThat(stockMap.get("PROD-002").get()).isEqualTo(9); // 10 - 1 = 9
        
        // 5-1. TOSS_PAYMENT는 주문 생성 시점에 결제 API를 호출하지 않음
        verify(paymentServiceClient, never()).requestDepositPayment(any());
        verify(paymentServiceClient, never()).requestTossPayment(any());

        // 5-2. TOSS_PAYMENT는 주문 생성 시점에 주문 완료 이벤트를 발행하지 않음 (CREATED 상태이므로)
        verify(orderEventService, never()).publishOrderCompletedEvents(any(), any(), anyString());

        // ========== 2단계: 토스 결제 완료 처리 ==========
        String paymentKey = "test_payment_key_12345";
        
        // Mock 재설정: completePaymentWithKey에서 사용할 Mock
        doNothing().when(paymentServiceClient).requestTossPayment(any());

        // when - UI에서 결제 완료 후 completePaymentWithKey 호출 (모킹)
        log.info("*****************************before completePaymentWithKey (TOSS_PAYMENT)*****************************");
        orderService.completePaymentWithKey(result.orderCode(), paymentKey);
        log.info("*****************************after completePaymentWithKey (TOSS_PAYMENT)*****************************");

        // then - 결제 완료 후 검증
        // 6. 주문 상태가 COMPLETED로 변경되었는지 확인
        Order completedOrder = orderJpaRepository.findByCode(result.orderCode());
        log.info("*****************************completedOrder (TOSS_PAYMENT)*****************************");
        assertThat(completedOrder).isNotNull();
        assertThat(completedOrder.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(completedOrder.getTotalPrice()).isEqualTo(35000);
        assertThat(completedOrder.getBuyerId()).isEqualTo(1L);
        assertThat(completedOrder.getBuyerCode()).isEqualTo("USER-001");

        // 7. 주문 이력 확인 (CREATE + STATUS_CHANGE)
        List<OrderHistoryEntity> completedOrderHistories = orderHistoryJpaRepository.findByOrderCode(completedOrder.getCode());
        assertThat(completedOrderHistories).hasSizeGreaterThanOrEqualTo(2); // CREATE + STATUS_CHANGE

        // 결제 성공 이력 확인
        OrderHistoryEntity tossPaymentSuccessHistory = completedOrderHistories.stream()
                .filter(history -> history.getActionType() == OrderHistoryActionType.STATUS_CHANGE)
                .filter(history -> history.getCurrentStatus() == OrderStatus.COMPLETED)
                .findFirst()
                .orElse(null);
        log.info("*****************************tossPaymentSuccessHistory (TOSS_PAYMENT)*****************************");
        assertThat(tossPaymentSuccessHistory).isNotNull();
        assertThat(tossPaymentSuccessHistory.getOrderCode()).isEqualTo(completedOrder.getCode());
        assertThat(tossPaymentSuccessHistory.getCurrentStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(tossPaymentSuccessHistory.getPreviousStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(tossPaymentSuccessHistory.getReason()).contains("토스");

        // 8. Payment API 호출 검증 (completePaymentWithKey에서 호출됨)
        ArgumentCaptor<OrderPaymentRequest> paymentRequestCaptor = ArgumentCaptor.forClass(OrderPaymentRequest.class);
        verify(paymentServiceClient, times(1)).requestTossPayment(paymentRequestCaptor.capture());

        OrderPaymentRequest capturedPaymentRequest = paymentRequestCaptor.getValue();
        assertThat(capturedPaymentRequest.orderId()).isEqualTo(completedOrder.getId());
        assertThat(capturedPaymentRequest.orderCode()).isEqualTo(completedOrder.getCode());
        assertThat(capturedPaymentRequest.buyerId()).isEqualTo(1L);
        assertThat(capturedPaymentRequest.buyerCode()).isEqualTo("USER-001");
        assertThat(capturedPaymentRequest.paidAmount()).isEqualTo(35000);
        assertThat(capturedPaymentRequest.paidType()).isEqualTo(PaidType.TOSS_PAYMENT);
        assertThat(capturedPaymentRequest.paymentKey()).isEqualTo(paymentKey);

        // 9. 주문 완료 이벤트 발행 검증 (결제 완료 후)
        List<OrderItem> completedOrderItems = orderItemJpaRepository.findByOrderId(completedOrder.getId());
        verify(orderEventService, times(1)).publishOrderCompletedEvents(
                eq(completedOrder),
                eq(completedOrderItems),
                eq("USER-001")
        );

        log.info("*****************************after assertions (TOSS_PAYMENT)*****************************");
    }

}
