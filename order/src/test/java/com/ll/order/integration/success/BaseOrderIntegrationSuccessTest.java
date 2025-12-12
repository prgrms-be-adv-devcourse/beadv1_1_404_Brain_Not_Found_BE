package com.ll.order.integration.success;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

// 공통 설정 및 헬퍼 메서드 제공
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public abstract class BaseOrderIntegrationSuccessTest {

    @Autowired
    protected OrderService orderService;

    @Autowired
    protected OrderJpaRepository orderJpaRepository;

    @Autowired
    protected OrderItemJpaRepository orderItemJpaRepository;

    @Autowired
    protected OrderHistoryJpaRepository orderHistoryJpaRepository;

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

    @MockitoBean
    protected CompensationService compensationService;

    @MockitoBean
    protected OrderEventService orderEventService;

    // 테스트 데이터
    protected UserResponse testUser;
    protected ProductResponse testProduct;
    protected ProductResponse testProduct2;

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
                .name("테스트상품-1")
                .sellerCode("SELLER-001")
                .sellerName("판매자1")
                .quantity(10) // 초기 재고
                .price(10000)
                .status(ProductStatus.ON_SALE)
                .images(null)
                .build();

        // 두 번째 테스트 상품 설정 (재고: 10개)
        testProduct2 = ProductResponse.builder()
                .id(2L)
                .code("PROD-002")
                .name("테스트상품-2")
                .sellerCode("SELLER-001")
                .sellerName("판매자1")
                .quantity(10) // 초기 재고
                .price(15000)
                .status(ProductStatus.ON_SALE)
                .images(null)
                .build();
    }
}

