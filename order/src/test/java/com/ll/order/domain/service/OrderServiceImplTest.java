package com.ll.order.domain.service;

import com.ll.order.domain.client.CartServiceClient;
import com.ll.order.domain.client.PaymentServiceClient;
import com.ll.order.domain.client.ProductServiceClient;
import com.ll.order.domain.client.UserServiceClient;
import com.ll.order.domain.messaging.producer.OrderEventProducer;
import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.entity.OrderItem;
import com.ll.order.domain.model.enums.order.OrderStatus;
import com.ll.order.domain.model.enums.order.OrderType;
import com.ll.order.domain.model.enums.payment.PaidType;
import com.ll.order.domain.model.enums.product.ProductStatus;
import com.ll.order.domain.model.enums.user.AccountStatus;
import com.ll.order.domain.model.enums.user.Grade;
import com.ll.order.domain.model.enums.user.Role;
import com.ll.order.domain.model.enums.user.SocialProvider;
import com.ll.order.domain.model.vo.request.*;
import com.ll.order.domain.model.vo.response.cart.CartItemInfo;
import com.ll.order.domain.model.vo.response.cart.CartItemsResponse;
import com.ll.order.domain.model.vo.response.order.*;
import com.ll.order.domain.model.vo.response.product.ProductImageDto;
import com.ll.order.domain.model.vo.response.product.ProductResponse;
import com.ll.order.domain.model.vo.response.user.UserResponse;
import com.ll.order.domain.repository.OrderItemJpaRepository;
import com.ll.order.domain.repository.OrderJpaRepository;
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
class OrderServiceImplTest { // BDD, nested test, slice test

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

    @Mock
    private OrderEventProducer orderEventProducer;

    @InjectMocks
    private OrderServiceImpl orderService;

    private UserResponse testUserInfo;
    private ProductResponse testProductInfo;
    private CartItemsResponse testCartInfo;

    @BeforeEach
    void setUp() {
        testUserInfo = new UserResponse(
                1L,
                "USER-001",  // code 필드 추가
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
                null,  // createAt
                null   // updatedAt (address 필드 제거)
        );

        testProductInfo = ProductResponse.builder()
                .id(1L)
                .code("PROD-001")
                .name("테스트상품")
                .sellerCode("SELLER-001")
                .sellerName("판매자1")
                .quantity(1)
                .price(10000)
                .status(ProductStatus.ON_SALE)
                .images(null)
                .build();

        List<CartItemInfo> cartItems = new ArrayList<>();
        cartItems.add(new CartItemInfo("CART-ITEM-001", 1L, "PROD-001", 2, 20000));
        testCartInfo = new CartItemsResponse("CART-001", 20000, cartItems);
    }

    @DisplayName("장바구니 주문 생성 - 단일 상품")
    @Test
    void createCartItemOrder_Success() {
        // given
        String userCode = "USER-001";
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
        OrderCreateResponse result = orderService.createCartItemOrder(request, userCode);

        // then
        assertThat(result).isNotNull();
        assertThat(result.orderCode()).isNotNull();
        assertThat(result.buyerId()).isEqualTo(1L);
        assertThat(result.totalPrice()).isEqualTo(20000);
        assertThat(result.orderType()).isEqualTo(OrderType.ONLINE);
        assertThat(result.orderStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(result.address()).isEqualTo("서울시 강남구");
        assertThat(result.orderItems()).hasSize(1);
        OrderCreateResponse.OrderItemInfo itemInfo = result.orderItems().get(0);
        assertThat(itemInfo.productId()).isEqualTo(1L);
        assertThat(itemInfo.sellerCode()).isEqualTo("SELLER-001");
        assertThat(itemInfo.quantity()).isEqualTo(2);
        assertThat(itemInfo.price()).isEqualTo(10000); // totalPrice(20000) / quantity(2) = 10000

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderJpaRepository, times(2)).save(orderCaptor.capture()); // 주문 생성 시 1번, 결제 성공 후 상태 변경 시 1번
        Order capturedOrder = orderCaptor.getValue();

        assertThat(orderCaptor.getAllValues()).hasSize(2);
        assertThat(capturedOrder.getBuyerId()).isEqualTo(1L);
        assertThat(capturedOrder.getTotalPrice()).isEqualTo(20000);
        assertThat(capturedOrder.getOrderType()).isEqualTo(OrderType.ONLINE);
        assertThat(capturedOrder.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(capturedOrder.getAddress()).isEqualTo("서울시 강남구");
        assertThat(capturedOrder.getCode()).isNotNull();

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
        List<CartItemInfo> multipleCartItems = new ArrayList<>();
        multipleCartItems.add(new CartItemInfo("CART-ITEM-001", 1L, "PROD-001", 2, 20000)); // quantity 2, totalPrice 20000
        multipleCartItems.add(new CartItemInfo("CART-ITEM-002", 2L, "PROD-002", 1, 15000)); // quantity 1, totalPrice 15000
        multipleCartItems.add(new CartItemInfo("CART-ITEM-003", 3L, "PROD-003", 3, 15000)); // quantity 3, totalPrice 15000
        CartItemsResponse multipleItemCart = new CartItemsResponse("CART-002", 50000, multipleCartItems);

        ProductResponse product1 = ProductResponse.builder()
                .id(1L)
                .code("PROD-001")
                .name("상품1")
                .sellerCode("SELLER-001")
                .sellerName("판매자1")
                .quantity(2)
                .price(10000)
                .status(ProductStatus.ON_SALE)
                .images(null)
                .build();
        ProductResponse product2 = ProductResponse.builder()
                .id(2L)
                .code("PROD-002")
                .name("상품2")
                .sellerCode("SELLER-002")
                .sellerName("판매자2")
                .quantity(1)
                .price(15000)
                .status(ProductStatus.ON_SALE)
                .images(null)
                .build();
        ProductResponse product3 = ProductResponse.builder()
                .id(3L)
                .code("PROD-003")
                .name("상품3")
                .sellerCode("SELLER-003")
                .sellerName("판매자3")
                .quantity(3)
                .price(5000)
                .status(ProductStatus.ON_SALE)
                .images(null)
                .build();

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
        String userCode = "USER-001";
        OrderCreateResponse result = orderService.createCartItemOrder(request, userCode);

        // then
        assertThat(result).isNotNull();
        assertThat(result.orderCode()).isNotNull();
        assertThat(result.buyerId()).isEqualTo(1L);
        assertThat(result.totalPrice()).isEqualTo(50000);
        assertThat(result.orderType()).isEqualTo(OrderType.ONLINE);
        assertThat(result.orderStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(result.orderItems()).hasSize(3);
        
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderJpaRepository, times(2)).save(orderCaptor.capture()); // 주문 생성 시 1번, 결제 성공 후 상태 변경 시 1번
        Order capturedOrder = orderCaptor.getValue();

        assertThat(orderCaptor.getAllValues()).hasSize(2);
        assertThat(capturedOrder.getBuyerId()).isEqualTo(1L);
        assertThat(capturedOrder.getTotalPrice()).isEqualTo(50000);
        assertThat(capturedOrder.getOrderType()).isEqualTo(OrderType.ONLINE);
        assertThat(capturedOrder.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);

        // ProductService가 3번 호출되었는지
        ArgumentCaptor<String> productCodeCaptor = ArgumentCaptor.forClass(String.class);
        verify(productServiceClient, times(3)).getProductByCode(productCodeCaptor.capture());
        List<String> capturedProductCodes = productCodeCaptor.getAllValues();

        assertThat(capturedProductCodes).hasSize(3);
        assertThat(capturedProductCodes).containsExactlyInAnyOrder("PROD-001", "PROD-002", "PROD-003");
        
        // OrderItems 검증
        assertThat(result.orderItems().get(0).productId()).isEqualTo(1L);
        assertThat(result.orderItems().get(0).sellerCode()).isEqualTo("SELLER-001");
        assertThat(result.orderItems().get(1).productId()).isEqualTo(2L);
        assertThat(result.orderItems().get(1).sellerCode()).isEqualTo("SELLER-002");
        assertThat(result.orderItems().get(2).productId()).isEqualTo(3L);
        assertThat(result.orderItems().get(2).sellerCode()).isEqualTo("SELLER-003");
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
        String userCode = "USER-001";
        OrderCreateResponse result = orderService.createDirectOrder(request, userCode);

        // then
        assertThat(result).isNotNull();
        assertThat(result.orderCode()).isNotNull();
        assertThat(result.buyerId()).isEqualTo(1L);
        assertThat(result.totalPrice()).isEqualTo(20000);
        assertThat(result.orderStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(result.orderType()).isEqualTo(OrderType.ONLINE);
        assertThat(result.address()).isEqualTo("서울시 강남구");
        assertThat(result.orderItems()).hasSize(1);
        OrderCreateResponse.OrderItemInfo orderItemInfo = result.orderItems().get(0);
        assertThat(orderItemInfo.productId()).isEqualTo(1L);
        assertThat(orderItemInfo.sellerCode()).isEqualTo("SELLER-001");
        assertThat(orderItemInfo.quantity()).isEqualTo(2);
        assertThat(orderItemInfo.price()).isEqualTo(10000);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderJpaRepository, times(2)).save(orderCaptor.capture()); // 주문 생성 시 1번, 결제 성공 후 상태 변경 시 1번
        Order capturedOrder = orderCaptor.getValue();

        assertThat(orderCaptor.getAllValues()).hasSize(2);
        assertThat(capturedOrder.getBuyerId()).isEqualTo(1L);
        assertThat(capturedOrder.getTotalPrice()).isEqualTo(20000);
        assertThat(capturedOrder.getOrderType()).isEqualTo(OrderType.ONLINE);
        assertThat(capturedOrder.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(capturedOrder.getAddress()).isEqualTo("서울시 강남구");
        assertThat(capturedOrder.getCode()).isNotNull();
        
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
        UserResponse userInfo = new UserResponse(
                1L,
                "USER-001",  // code 필드 추가
                "test_social_id",
                SocialProvider.KAKAO,
                "test@test.com",
                "사용자",
                Role.USER,
                null,
                5L,
                Grade.BRONZE,
                AccountStatus.ACTIVE,
                null,
                null,
                null,  // createAt
                null   // updatedAt (address 필드 제거)
        );
        when(userServiceClient.getUserByCode("USER-001")).thenReturn(userInfo);

        Order order = mock(Order.class);
        when(order.getId()).thenReturn(10L);
        when(order.getCode()).thenReturn("ORD-123");
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

        OrderItem orderItem = mock(OrderItem.class);
        when(orderItem.getCode()).thenReturn("ITEM-001");
        when(orderItem.getProductId()).thenReturn(1L);
        when(orderItem.getSellerCode()).thenReturn("SELLER-021");
        when(orderItem.getProductName()).thenReturn("상품1");
        when(orderItem.getQuantity()).thenReturn(2);
        when(orderItem.getPrice()).thenReturn(10000);

        when(orderJpaRepository.findByCode("ORD-999")).thenReturn(order);
        when(orderItemJpaRepository.findByOrderId(10L)).thenReturn(List.of(orderItem));
        List<ProductImageDto> images = List.of(
                ProductImageDto.builder()
                        .url("image.png")
                        .sequence(0)
                        .isMain(true)
                        .build()
        );
        when(orderItem.getProductCode()).thenReturn("PROD-001");
        when(productServiceClient.getProductByCode("PROD-001"))
                .thenReturn(ProductResponse.builder()
                        .id(1L)
                        .code("PROD-001")
                        .name("상품1")
                        .sellerCode("SELLER-021")
                        .sellerName("판매자1")
                        .quantity(10)
                        .price(10000)
                        .status(ProductStatus.ON_SALE)
                        .images(images)
                        .build());

        // when
        OrderDetailResponse response = orderService.findOrderDetails("ORD-999");

        // then
        assertThat(response.orderId()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(OrderStatus.DELIVERY);
        assertThat(response.totalPrice()).isEqualTo(45000);
        assertThat(response.userInfo().userId()).isEqualTo(5L);
        assertThat(response.userInfo().address()).isEqualTo("부산 해운대");
        assertThat(response.items()).hasSize(1);
        OrderDetailResponse.ItemInfo itemInfo = response.items().get(0);
        assertThat(itemInfo.orderItemCode()).isEqualTo("ITEM-001");
        assertThat(itemInfo.productId()).isEqualTo(1L);
        assertThat(itemInfo.sellerCode()).isEqualTo("SELLER-021");
        assertThat(itemInfo.productName()).isEqualTo("상품1");
        assertThat(itemInfo.quantity()).isEqualTo(2);
        assertThat(itemInfo.price()).isEqualTo(10000);
        assertThat(itemInfo.productImage()).isEqualTo("image.png");
    }

    @DisplayName("주문 상태 변경 - 성공")
    @Test
    void updateOrderStatus_Success() {
        // given
        Order mockOrder = mock(Order.class);
        when(mockOrder.getCode()).thenReturn("ORD-111");
        when(mockOrder.getUpdatedAt()).thenReturn(null);
        when(orderJpaRepository.findByCode("ORD-111")).thenReturn(mockOrder);
        when(orderJpaRepository.save(mockOrder)).thenReturn(mockOrder);
        
        // 상태 변경 전에는 CREATED, changeStatus 호출 후에는 PAID 반환
        when(mockOrder.getOrderStatus()).thenReturn(OrderStatus.CREATED);
        doAnswer(invocation -> {
            // changeStatus 호출 시 getOrderStatus가 PAID를 반환하도록 변경
            when(mockOrder.getOrderStatus()).thenReturn(OrderStatus.PAID);
            return null;
        }).when(mockOrder).changeStatus(OrderStatus.PAID);

        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(OrderStatus.PAID);

        // when
        String userCode = "USER-001";
        OrderStatusUpdateResponse response = orderService.updateOrderStatus("ORD-111", request, userCode);

        // then
        assertThat(response.orderCode()).isEqualTo("ORD-111");
        assertThat(response.status()).isEqualTo(OrderStatus.PAID);
        verify(mockOrder).changeStatus(OrderStatus.PAID);
        verify(orderJpaRepository).save(mockOrder);
    }

    @DisplayName("주문 검증 - 성공")
    @Test
    void validateOrder_Success() {
        // given
        ProductRequest productRequest1 = new ProductRequest("PROD-001", 2, 10000, null);
        ProductRequest productRequest2 = new ProductRequest("PROD-002", 1, 5000, null);
        OrderValidateRequest request = new OrderValidateRequest(
                "USER-001",
                List.of(productRequest1, productRequest2)
        );

        when(productServiceClient.getProductByCode("PROD-001"))
                .thenReturn(ProductResponse.builder()
                        .id(11L)
                        .code("PROD-001")
                        .name("상품1")
                        .sellerCode("SELLER-021")
                        .sellerName("판매자1")
                        .quantity(5)
                        .price(10000)
                        .status(ProductStatus.ON_SALE)
                        .images(null)
                        .build());
        when(productServiceClient.getProductByCode("PROD-002"))
                .thenReturn(ProductResponse.builder()
                        .id(12L)
                        .code("PROD-002")
                        .name("상품2")
                        .sellerCode("SELLER-022")
                        .sellerName("판매자2")
                        .quantity(3)
                        .price(5000)
                        .status(ProductStatus.ON_SALE)
                        .images(null)
                        .build());

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

