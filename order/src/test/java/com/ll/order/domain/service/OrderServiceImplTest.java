package com.ll.order.domain.service;

import com.ll.order.domain.client.CartServiceClient;
import com.ll.order.domain.client.PaymentServiceClient;
import com.ll.order.domain.client.ProductServiceClient;
import com.ll.order.domain.client.UserServiceClient;
import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.entity.OrderItem;
import com.ll.order.domain.model.enums.OrderStatus;
import com.ll.order.domain.model.enums.OrderType;
import com.ll.order.domain.model.enums.ProductSaleStatus;
import com.ll.order.domain.model.vo.request.*;
import com.ll.order.domain.model.vo.response.*;
import com.ll.order.domain.repository.OrderItemJpaRepository;
import com.ll.order.domain.repository.OrderJpaRepository;
import com.ll.payment.model.enums.PaidType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 테스트")
class OrderServiceImplTest {

    @Mock
    private OrderJpaRepository orderJpaRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private CartServiceClient cartServiceClient;

    @Mock
    private PaymentServiceClient paymentServiceClient;

    @Mock
    private OrderItemJpaRepository orderItemJpaRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private ClientResponse testUserInfo;
    private ProductResponse testProductInfo;
    private CartResponse testCartInfo;

    @BeforeEach
    void setUp() {
        testUserInfo = new ClientResponse(1L, "홍길동", "서울시 강남구");

        testProductInfo = new ProductResponse(1L, 10L, "테스트상품", 1, 10000, ProductSaleStatus.ON_SALE, null);

        List<CartResponse.CartItemResponse> cartItems = new ArrayList<>();
        cartItems.add(new CartResponse.CartItemResponse(1L, "PROD-001", 2, 10000));
        testCartInfo = new CartResponse(1L, "CART-001", 1L, cartItems, 20000);
    }

    @DisplayName("장바구니 주문 생성 - 단일 상품")
    @Test
    void createCartItemOrder_Success() {
        // given
        OrderCartItemRequest request = new OrderCartItemRequest(
                "USER-001",
                "CART-001",
                "홍길동",
                "서울시 강남구",
                new ArrayList<>(),
                20000,
                OrderType.ONLINE,
                PaidType.DEPOSIT,
                null
        );

        when(userServiceClient.getUserByCode("USER-001")).thenReturn(testUserInfo);
        when(cartServiceClient.getCartByCode("CART-001")).thenReturn(testCartInfo);
        when(productServiceClient.getProductByCode("PROD-001")).thenReturn(testProductInfo);
        when(orderJpaRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        List<OrderItem> storedOrderItems = new ArrayList<>();
        doAnswer(invocation -> {
            List<OrderItem> items = invocation.getArgument(0);
            storedOrderItems.clear();
            storedOrderItems.addAll(items);
            return items;
        }).when(orderItemJpaRepository).saveAll(anyList());
        when(orderItemJpaRepository.findByOrderId(any())).thenAnswer(invocation -> storedOrderItems);

        // when
        OrderCreateResponse result = orderService.createCartItemOrder(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.orderCode()).startsWith("ORD-");
        assertThat(result.buyerId()).isEqualTo(1L);
        assertThat(result.totalPrice()).isEqualTo(20000);
        assertThat(result.orderType()).isEqualTo(OrderType.ONLINE);
        assertThat(result.orderStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(result.address()).isEqualTo("서울시 강남구");
        assertThat(result.orderItems()).hasSize(1);
        OrderCreateResponse.OrderItemInfo itemInfo = result.orderItems().get(0);
        assertThat(itemInfo.productId()).isEqualTo(1L);
        assertThat(itemInfo.sellerId()).isEqualTo(10L);
        assertThat(itemInfo.quantity()).isEqualTo(2);
        assertThat(itemInfo.price()).isEqualTo(10000);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderJpaRepository).save(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();

        assertThat(orderCaptor.getAllValues()).hasSize(1);
        assertThat(capturedOrder.getBuyerId()).isEqualTo(1L);
        assertThat(capturedOrder.getTotalPrice()).isEqualTo(20000);
        assertThat(capturedOrder.getOrderType()).isEqualTo(OrderType.ONLINE);
        assertThat(capturedOrder.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(capturedOrder.getAddress()).isEqualTo("서울시 강남구");
        assertThat(capturedOrder.getOrderCode()).startsWith("ORD-");

        ArgumentCaptor<String> userCodeCaptor = ArgumentCaptor.forClass(String.class);
        verify(userServiceClient).getUserByCode(userCodeCaptor.capture());
        assertThat(userCodeCaptor.getAllValues()).hasSize(1);
        assertThat(userCodeCaptor.getValue()).isEqualTo("USER-001");
        
        ArgumentCaptor<String> cartCodeCaptor = ArgumentCaptor.forClass(String.class);
        verify(cartServiceClient).getCartByCode(cartCodeCaptor.capture());
        assertThat(cartCodeCaptor.getAllValues()).hasSize(1);
        assertThat(cartCodeCaptor.getValue()).isEqualTo("CART-001");
        
        ArgumentCaptor<String> productCodeCaptor = ArgumentCaptor.forClass(String.class);
        verify(productServiceClient).getProductByCode(productCodeCaptor.capture());
        assertThat(productCodeCaptor.getAllValues()).hasSize(1);
        assertThat(productCodeCaptor.getValue()).isEqualTo("PROD-001");
    }

    @DisplayName("장바구니 주문 생성 - 여러 상품")
    @Test
    void createCartItemOrder_WithMultipleProducts_Success() {
        // given
        List<CartResponse.CartItemResponse> multipleCartItems = new ArrayList<>();
        multipleCartItems.add(new CartResponse.CartItemResponse(1L, "PROD-001", 2, 10000));
        multipleCartItems.add(new CartResponse.CartItemResponse(2L, "PROD-002", 1, 15000));
        multipleCartItems.add(new CartResponse.CartItemResponse(3L, "PROD-003", 3, 5000));
        CartResponse multipleItemCart = new CartResponse(1L, "CART-002", 1L, multipleCartItems, 50000);

        ProductResponse product1 = new ProductResponse(1L, 10L, "상품1", 2, 10000, ProductSaleStatus.ON_SALE, null);
        ProductResponse product2 = new ProductResponse(2L, 20L, "상품2", 1, 15000, ProductSaleStatus.ON_SALE, null);
        ProductResponse product3 = new ProductResponse(3L, 30L, "상품3", 3, 5000, ProductSaleStatus.ON_SALE, null);

        OrderCartItemRequest request = new OrderCartItemRequest(
                "USER-001",
                "CART-002",
                "홍길동",
                "서울시 강남구",
                new ArrayList<>(),
                50000,
                OrderType.ONLINE,
                PaidType.DEPOSIT,
                null
        );

        when(userServiceClient.getUserByCode("USER-001")).thenReturn(testUserInfo);
        when(cartServiceClient.getCartByCode("CART-002")).thenReturn(multipleItemCart);
        when(productServiceClient.getProductByCode("PROD-001")).thenReturn(product1);
        when(productServiceClient.getProductByCode("PROD-002")).thenReturn(product2);
        when(productServiceClient.getProductByCode("PROD-003")).thenReturn(product3);
        when(orderJpaRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        List<OrderItem> storedOrderItems = new ArrayList<>();
        doAnswer(invocation -> {
            List<OrderItem> items = invocation.getArgument(0);
            storedOrderItems.clear();
            storedOrderItems.addAll(items);
            return items;
        }).when(orderItemJpaRepository).saveAll(anyList());
        when(orderItemJpaRepository.findByOrderId(any())).thenAnswer(invocation -> storedOrderItems);

        // when
        OrderCreateResponse result = orderService.createCartItemOrder(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.orderCode()).startsWith("ORD-");
        assertThat(result.buyerId()).isEqualTo(1L);
        assertThat(result.totalPrice()).isEqualTo(50000);
        assertThat(result.orderType()).isEqualTo(OrderType.ONLINE);
        assertThat(result.orderStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(result.orderItems()).hasSize(3);
        
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderJpaRepository).save(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();

        assertThat(orderCaptor.getAllValues()).hasSize(1);
        assertThat(capturedOrder.getBuyerId()).isEqualTo(1L);
        assertThat(capturedOrder.getTotalPrice()).isEqualTo(50000);
        assertThat(capturedOrder.getOrderType()).isEqualTo(OrderType.ONLINE);
        assertThat(capturedOrder.getOrderStatus()).isEqualTo(OrderStatus.CREATED);

        // ProductService가 3번 호출되었는지
        ArgumentCaptor<String> productCodeCaptor = ArgumentCaptor.forClass(String.class);
        verify(productServiceClient, times(3)).getProductByCode(productCodeCaptor.capture());
        List<String> capturedProductCodes = productCodeCaptor.getAllValues();

        assertThat(capturedProductCodes).hasSize(3);
        assertThat(capturedProductCodes).containsExactlyInAnyOrder("PROD-001", "PROD-002", "PROD-003");
        
        // OrderItems 검증
        assertThat(result.orderItems().get(0).productId()).isEqualTo(1L);
        assertThat(result.orderItems().get(0).sellerId()).isEqualTo(10L);
        assertThat(result.orderItems().get(1).productId()).isEqualTo(2L);
        assertThat(result.orderItems().get(1).sellerId()).isEqualTo(20L);
        assertThat(result.orderItems().get(2).productId()).isEqualTo(3L);
        assertThat(result.orderItems().get(2).sellerId()).isEqualTo(30L);
    }

    @DisplayName("직접 주문 생성")
    @Test
    void createDirectOrder_Success() {
        // given
        OrderDirectRequest request = new OrderDirectRequest(
                "USER-001",
                "PROD-001",
                2,
                "서울시 강남구",
                OrderType.ONLINE,
                PaidType.DEPOSIT,
                null
        );

        when(userServiceClient.getUserByCode("USER-001")).thenReturn(testUserInfo);
        when(productServiceClient.getProductByCode("PROD-001")).thenReturn(testProductInfo);
        when(orderJpaRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        List<OrderItem> storedOrderItems = new ArrayList<>();
        doAnswer(invocation -> {
            OrderItem item = invocation.getArgument(0);
            storedOrderItems.clear();
            storedOrderItems.add(item);
            return item;
        }).when(orderItemJpaRepository).save(any(OrderItem.class));
        when(orderItemJpaRepository.findByOrderId(any())).thenAnswer(invocation -> storedOrderItems);
        when(paymentServiceClient.requestDepositPayment(any(OrderPaymentRequest.class))).thenReturn("OK");

        // when
        OrderCreateResponse result = orderService.createDirectOrder(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.orderCode()).startsWith("ORD-");
        assertThat(result.buyerId()).isEqualTo(1L);
        assertThat(result.totalPrice()).isEqualTo(20000);
        assertThat(result.orderStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(result.orderType()).isEqualTo(OrderType.ONLINE);
        assertThat(result.address()).isEqualTo("서울시 강남구");
        assertThat(result.orderItems()).hasSize(1);
        OrderCreateResponse.OrderItemInfo orderItemInfo = result.orderItems().get(0);
        assertThat(orderItemInfo.productId()).isEqualTo(1L);
        assertThat(orderItemInfo.sellerId()).isEqualTo(10L);
        assertThat(orderItemInfo.quantity()).isEqualTo(2);
        assertThat(orderItemInfo.price()).isEqualTo(10000);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderJpaRepository).save(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();
        
        assertThat(orderCaptor.getAllValues()).hasSize(1);
        assertThat(capturedOrder.getBuyerId()).isEqualTo(1L);
        assertThat(capturedOrder.getTotalPrice()).isEqualTo(20000);
        assertThat(capturedOrder.getOrderType()).isEqualTo(OrderType.ONLINE);
        assertThat(capturedOrder.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(capturedOrder.getAddress()).isEqualTo("서울시 강남구");
        assertThat(capturedOrder.getOrderCode()).startsWith("ORD-");
        
        // Client 호출 검증
        ArgumentCaptor<String> userCodeCaptor = ArgumentCaptor.forClass(String.class);
        verify(userServiceClient).getUserByCode(userCodeCaptor.capture());
        assertThat(userCodeCaptor.getAllValues()).hasSize(1);
        assertThat(userCodeCaptor.getValue()).isEqualTo("USER-001");
        
        ArgumentCaptor<String> productCodeCaptor = ArgumentCaptor.forClass(String.class);
        verify(productServiceClient).getProductByCode(productCodeCaptor.capture());
        assertThat(productCodeCaptor.getAllValues()).hasSize(1);
        assertThat(productCodeCaptor.getValue()).isEqualTo("PROD-001");

        ArgumentCaptor<OrderPaymentRequest> paymentCaptor = ArgumentCaptor.forClass(OrderPaymentRequest.class);
        verify(paymentServiceClient).requestDepositPayment(paymentCaptor.capture());
        OrderPaymentRequest paymentRequest = paymentCaptor.getValue();
        assertThat(paymentRequest.orderId()).isEqualTo(capturedOrder.getId());
        assertThat(paymentRequest.buyerId()).isEqualTo(capturedOrder.getBuyerId());
        assertThat(paymentRequest.paidAmount()).isEqualTo(capturedOrder.getTotalPrice());
        assertThat(paymentRequest.paidType()).isEqualTo(PaidType.DEPOSIT);
    }

    @DisplayName("주문 목록 조회 - 성공")
    @Test
    void findAllOrders_Success() {
        // given
        ClientResponse userInfo = new ClientResponse(1L, "사용자", "서울");
        when(userServiceClient.getUserByCode("USER-001")).thenReturn(userInfo);

        Order order = mock(Order.class);
        when(order.getId()).thenReturn(10L);
        when(order.getOrderCode()).thenReturn("ORD-123");
        when(order.getOrderStatus()).thenReturn(OrderStatus.PAID);
        when(order.getTotalPrice()).thenReturn(30000);
        when(order.getBuyerId()).thenReturn(1L);
        when(order.getAddress()).thenReturn("서울");

        Page<Order> orderPage = new PageImpl<>(List.of(order), PageRequest.of(0, 5), 1);
        when(orderJpaRepository.findByBuyerId(eq(1L), any(Pageable.class))).thenReturn(orderPage);

        Pageable pageable = PageRequest.of(0, 5);

        // when
        OrderPageResponse response = orderService.findAllOrders("USER-001", null, pageable);

        // then
        assertThat(response.orders()).hasSize(1);
        OrderPageResponse.OrderInfo orderInfo = response.orders().get(0);
        assertThat(orderInfo.orderCode()).isEqualTo("ORD-123");
        assertThat(orderInfo.status()).isEqualTo(OrderStatus.PAID);
        assertThat(orderInfo.totalPrice()).isEqualTo(30000);
        assertThat(orderInfo.userInfo().id()).isEqualTo(1L);
        assertThat(response.pageInfo().currentPage()).isEqualTo(0);
        assertThat(response.pageInfo().totalElements()).isEqualTo(1);
    }

    @DisplayName("주문 상세 조회 - 성공")
    @Test
    void findOrderDetails_Success() {
        // given
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(10L);
        when(order.getOrderStatus()).thenReturn(OrderStatus.DELIVERY);
        when(order.getTotalPrice()).thenReturn(45000);
        when(order.getBuyerId()).thenReturn(5L);
        when(order.getAddress()).thenReturn("부산 해운대");

        when(orderJpaRepository.findByOrderCode("ORD-999")).thenReturn(order);

        // when
        OrderDetailResponse response = orderService.findOrderDetails("ORD-999");

        // then
        assertThat(response.orderId()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(OrderStatus.DELIVERY);
        assertThat(response.totalPrice()).isEqualTo(45000);
        assertThat(response.userInfo().userId()).isEqualTo(5L);
        assertThat(response.userInfo().address()).isEqualTo("부산 해운대");
    }

    @DisplayName("주문 상태 변경 - 성공")
    @Test
    void updateOrderStatus_Success() {
        // given
        Order order = Order.create("ORD-111", 1L, OrderType.ONLINE, "서울시 마포구");
        when(orderJpaRepository.findByOrderCode("ORD-111")).thenReturn(order);

        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(OrderStatus.PAID);

        // when
        OrderStatusUpdateResponse response = orderService.updateOrderStatus("ORD-111", request);

        // then
        assertThat(response.orderCode()).isEqualTo("ORD-111");
        assertThat(response.status()).isEqualTo(OrderStatus.PAID);
        assertThat(response.updatedAt()).isNull();
    }

    @DisplayName("주문 검증 - 성공")
    @Test
    void validateOrder_Success() {
        // given
        when(userServiceClient.getUserByCode("USER-001"))
                .thenReturn(new ClientResponse(1L, "사용자", "서울시 강남구"));

        ProductRequest productRequest1 = new ProductRequest("PROD-001", 2, 10000, null);
        ProductRequest productRequest2 = new ProductRequest("PROD-002", 1, 5000, null);
        OrderValidateRequest request = new OrderValidateRequest(
                "USER-001",
                List.of(productRequest1, productRequest2)
        );

        when(productServiceClient.getProductByCode("PROD-001"))
                .thenReturn(new ProductResponse(11L, 21L, "상품1", 5, 10000, ProductSaleStatus.ON_SALE, null));
        when(productServiceClient.getProductByCode("PROD-002"))
                .thenReturn(new ProductResponse(12L, 22L, "상품2", 3, 5000, ProductSaleStatus.ON_SALE, null));

        // when
        OrderValidateResponse response = orderService.validateOrder(request);

        // then
        assertThat(response.buyerCode()).isEqualTo("USER-001");
        assertThat(response.totalQuantity()).isEqualTo(3);
        assertThat(response.totalAmount()).isEqualTo(BigDecimal.valueOf(25000));
        assertThat(response.items()).hasSize(2);
        assertThat(response.items().get(0).productCode()).isEqualTo("PROD-001");
        assertThat(response.items().get(0).price()).isEqualTo(10000);
        assertThat(response.items().get(1).productCode()).isEqualTo("PROD-002");
        assertThat(response.items().get(1).price()).isEqualTo(5000);
    }
}

