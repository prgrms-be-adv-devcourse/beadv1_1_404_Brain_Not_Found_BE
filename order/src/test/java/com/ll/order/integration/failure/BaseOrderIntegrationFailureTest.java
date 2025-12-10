package com.ll.order.integration.failure;

import com.ll.order.domain.client.CartServiceClient;
import com.ll.order.domain.client.PaymentServiceClient;
import com.ll.order.domain.client.ProductServiceClient;
import com.ll.order.domain.client.UserServiceClient;
import com.ll.order.domain.messaging.producer.OrderEventProducer;
import com.ll.order.domain.model.enums.product.ProductStatus;
import com.ll.order.domain.model.enums.user.AccountStatus;
import com.ll.order.domain.model.enums.user.Grade;
import com.ll.order.domain.model.enums.user.Role;
import com.ll.order.domain.model.enums.user.SocialProvider;
import com.ll.order.domain.model.enums.order.OrderType;
import com.ll.order.domain.model.enums.payment.PaidType;
import com.ll.order.domain.model.vo.request.OrderCartItemRequest;
import com.ll.order.domain.model.vo.request.ProductRequest;
import com.ll.order.domain.model.vo.response.cart.CartItemInfo;
import com.ll.order.domain.model.vo.response.cart.CartItemsResponse;
import com.ll.order.domain.model.vo.response.product.ProductResponse;
import com.ll.order.domain.model.vo.response.user.UserResponse;
import com.ll.order.domain.repository.OrderHistoryJpaRepository;
import com.ll.order.domain.repository.OrderItemJpaRepository;
import com.ll.order.domain.repository.OrderJpaRepository;
import com.ll.order.domain.repository.TransactionTracingRepository;
import com.ll.order.domain.service.compensation.CompensationService;
import com.ll.order.domain.service.event.OrderEventService;
import com.ll.order.domain.service.order.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

// 공통 설정 및 헬퍼 메서드 제공
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public abstract class BaseOrderIntegrationFailureTest {

    @Autowired
    protected OrderService orderService;

    @Autowired
    protected OrderJpaRepository orderJpaRepository;

    @Autowired
    protected OrderItemJpaRepository orderItemJpaRepository;

    @Autowired
    protected OrderHistoryJpaRepository orderHistoryJpaRepository;

    @Autowired
    protected TransactionTracingRepository transactionTracingRepository;

    @Autowired
    protected PlatformTransactionManager transactionManager;

    protected TransactionTemplate transactionTemplate;

    @BeforeEach
    void initTransactionTemplate() {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionTemplate = new TransactionTemplate(transactionManager, definition);
    }

    // 외부 서비스 모킹 (다른 마이크로서비스)
    @MockitoBean
    protected UserServiceClient userServiceClient;

    @MockitoBean
    protected ProductServiceClient productServiceClient;

    @MockitoBean
    protected PaymentServiceClient paymentServiceClient;

    @MockitoBean
    protected CartServiceClient cartServiceClient;

    // 이벤트/메시징 관련 - 모킹 (외부 인프라: Kafka 등)
    @MockitoBean
    protected OrderEventProducer orderEventProducer;

    @Autowired
    protected CompensationService compensationService;

    @MockitoBean
    protected OrderEventService orderEventService;

    // ========== 공통 헬퍼 메서드 ==========

    protected UserResponse createTestUser() {
        return new UserResponse(
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
    }

    protected ProductResponse createTestProduct(String productCode, int quantity, int price) {
        return ProductResponse.builder()
                .id(Long.parseLong(productCode.split("-")[1]))
                .code(productCode)
                .name("테스트상품-" + productCode)
                .sellerCode("SELLER-001")
                .sellerName("판매자1")
                .quantity(quantity)
                .price(price)
                .status(ProductStatus.ON_SALE)
                .images(null)
                .build();
    }

    protected OrderCartItemRequest createCartOrderRequest() {
        List<ProductRequest> products = new ArrayList<>();
        products.add(new ProductRequest("PROD-001", 2, 10000, null));
        products.add(new ProductRequest("PROD-002", 1, 15000, null));

        return new OrderCartItemRequest(
                "CART-001",
                "홍길동",
                "서울시 강남구",
                products,
                35000, // 2 * 10000 + 1 * 15000
                OrderType.ONLINE,
                PaidType.DEPOSIT,
                null
        );
    }

    protected CartItemsResponse createCartResponse() {
        List<CartItemInfo> cartItems = new ArrayList<>();
        cartItems.add(new CartItemInfo("CART-ITEM-001", 1L, "PROD-001", 2, 20000));
        cartItems.add(new CartItemInfo("CART-ITEM-002", 2L, "PROD-002", 1, 15000));
        return new CartItemsResponse("CART-001", 35000, cartItems);
    }
}

