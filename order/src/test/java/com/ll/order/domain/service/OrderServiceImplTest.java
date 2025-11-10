package com.ll.order.domain.service;

import com.ll.order.domain.client.CartServiceClient;
import com.ll.order.domain.client.ProductServiceClient;
import com.ll.order.domain.client.UserServiceClient;
import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.entity.OrderItem;
import com.ll.order.domain.model.enums.OrderStatus;
import com.ll.order.domain.model.enums.OrderType;
import com.ll.order.domain.model.vo.request.OrderCartItemRequest;
import com.ll.order.domain.model.vo.request.OrderDirectRequest;
import com.ll.order.domain.model.vo.response.CartResponse;
import com.ll.order.domain.model.vo.response.ClientResponse;
import com.ll.order.domain.model.vo.response.OrderCreateResponse;
import com.ll.order.domain.model.vo.response.ProductResponse;
import com.ll.order.domain.repository.OrderJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private OrderServiceImpl orderService;

    private ClientResponse testUserInfo;
    private ProductResponse testProductInfo;
    private CartResponse testCartInfo;

    @BeforeEach
    void setUp() {
        testUserInfo = new ClientResponse(1L, "홍길동", "서울시 강남구");

        testProductInfo = new ProductResponse(1L, 10L, "테스트상품", 1, 10000, null);

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
                OrderType.ONLINE
        );

        when(userServiceClient.getUserByCode("USER-001")).thenReturn(testUserInfo);
        when(cartServiceClient.getCartByCode("CART-001")).thenReturn(testCartInfo);
        when(productServiceClient.getProductByCode("PROD-001")).thenReturn(testProductInfo);
        when(orderJpaRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
        
        assertThat(capturedOrder.getOrderItems()).hasSize(1);
        OrderItem capturedOrderItem = capturedOrder.getOrderItems().get(0);
        assertThat(capturedOrderItem.getOrder()).isEqualTo(capturedOrder);
        assertThat(capturedOrderItem.getProductId()).isEqualTo(1L);
        assertThat(capturedOrderItem.getSellerId()).isEqualTo(10L);
        assertThat(capturedOrderItem.getQuantity()).isEqualTo(2);
        assertThat(capturedOrderItem.getPrice()).isEqualTo(10000);
        assertThat(capturedOrderItem.getOrderItemCode()).startsWith("ORD-ITEM-");
        
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

        ProductResponse product1 = new ProductResponse(1L, 10L, "상품1", 2, 10000, null);
        ProductResponse product2 = new ProductResponse(2L, 20L, "상품2", 1, 15000, null);
        ProductResponse product3 = new ProductResponse(3L, 30L, "상품3", 3, 5000, null);

        OrderCartItemRequest request = new OrderCartItemRequest(
                "USER-001",
                "CART-002",
                "홍길동",
                "서울시 강남구",
                new ArrayList<>(),
                50000,
                OrderType.ONLINE
        );

        when(userServiceClient.getUserByCode("USER-001")).thenReturn(testUserInfo);
        when(cartServiceClient.getCartByCode("CART-002")).thenReturn(multipleItemCart);
        when(productServiceClient.getProductByCode("PROD-001")).thenReturn(product1);
        when(productServiceClient.getProductByCode("PROD-002")).thenReturn(product2);
        when(productServiceClient.getProductByCode("PROD-003")).thenReturn(product3);
        when(orderJpaRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

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

        // OrderItem이 3개 포함되어 있는지 검증
        List<OrderItem> capturedOrderItems = capturedOrder.getOrderItems();
        assertThat(capturedOrderItems).hasSize(3);

        // 첫 번째 OrderItem 검증
        OrderItem firstItem = capturedOrderItems.get(0);
        assertThat(firstItem.getOrder()).isEqualTo(capturedOrder);
        assertThat(firstItem.getProductId()).isEqualTo(1L);
        assertThat(firstItem.getSellerId()).isEqualTo(10L);
        assertThat(firstItem.getQuantity()).isEqualTo(2);
        assertThat(firstItem.getPrice()).isEqualTo(10000);
        assertThat(firstItem.getOrderItemCode()).startsWith("ORD-ITEM-");

        // 두 번째 OrderItem 검증
        OrderItem secondItem = capturedOrderItems.get(1);
        assertThat(secondItem.getOrder()).isEqualTo(capturedOrder);
        assertThat(secondItem.getProductId()).isEqualTo(2L);
        assertThat(secondItem.getSellerId()).isEqualTo(20L);
        assertThat(secondItem.getQuantity()).isEqualTo(1);
        assertThat(secondItem.getPrice()).isEqualTo(15000);
        assertThat(secondItem.getOrderItemCode()).startsWith("ORD-ITEM-");

        // 세 번째 OrderItem 검증
        OrderItem thirdItem = capturedOrderItems.get(2);
        assertThat(thirdItem.getOrder()).isEqualTo(capturedOrder);
        assertThat(thirdItem.getProductId()).isEqualTo(3L);
        assertThat(thirdItem.getSellerId()).isEqualTo(30L);
        assertThat(thirdItem.getQuantity()).isEqualTo(3);
        assertThat(thirdItem.getPrice()).isEqualTo(5000);
        assertThat(thirdItem.getOrderItemCode()).startsWith("ORD-ITEM-");

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
                OrderType.ONLINE
        );

        when(userServiceClient.getUserByCode("USER-001")).thenReturn(testUserInfo);
        when(productServiceClient.getProductByCode("PROD-001")).thenReturn(testProductInfo);
        when(orderJpaRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
        
        assertThat(capturedOrder.getOrderItems()).hasSize(1);
        OrderItem capturedOrderItem = capturedOrder.getOrderItems().get(0);
        assertThat(capturedOrderItem.getOrder()).isEqualTo(capturedOrder);
        assertThat(capturedOrderItem.getProductId()).isEqualTo(1L);
        assertThat(capturedOrderItem.getSellerId()).isEqualTo(10L);
        assertThat(capturedOrderItem.getQuantity()).isEqualTo(2);
        assertThat(capturedOrderItem.getPrice()).isEqualTo(10000);
        assertThat(capturedOrderItem.getOrderItemCode()).startsWith("ORD-ITEM-");
        
        ArgumentCaptor<String> userCodeCaptor = ArgumentCaptor.forClass(String.class);
        verify(userServiceClient).getUserByCode(userCodeCaptor.capture());
        assertThat(userCodeCaptor.getAllValues()).hasSize(1);
        assertThat(userCodeCaptor.getValue()).isEqualTo("USER-001");
        
        ArgumentCaptor<String> productCodeCaptor = ArgumentCaptor.forClass(String.class);
        verify(productServiceClient).getProductByCode(productCodeCaptor.capture());
        assertThat(productCodeCaptor.getAllValues()).hasSize(1);
        assertThat(productCodeCaptor.getValue()).isEqualTo("PROD-001");
    }
}

