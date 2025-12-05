package com.ll.products.domain.product.service;

import com.ll.products.domain.product.exception.InsufficientInventoryException;
import com.ll.products.domain.product.model.entity.Product;
import com.ll.products.domain.product.model.entity.ProductStatus;
import com.ll.products.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import com.ll.products.domain.product.event.ProductEventListener;
import com.ll.products.domain.search.repository.ProductSearchRepository;
import com.ll.products.domain.search.service.ProductSearchService;
import com.ll.products.domain.search.controller.ProductSearchController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "spring.elasticsearch.uris=",  // Elasticsearch 비활성화
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchClientAutoConfiguration,org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration,org.springframework.boot.actuate.autoconfigure.elasticsearch.ElasticsearchRestHealthContributorAutoConfiguration",
    "management.health.elasticsearch.enabled=false"
})
@ActiveProfiles("test")
@DisplayName("상품 재고 동시성 테스트")
class ProductInventoryConcurrencyTest {

    private static final Logger log = LoggerFactory.getLogger(ProductInventoryConcurrencyTest.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    // Elasticsearch 관련 빈 모킹 (동시성 테스트에서는 불필요)
    @MockitoBean
    private ElasticsearchOperations elasticsearchOperations;

    @MockitoBean
    private ProductSearchRepository productSearchRepository;

    @MockitoBean
    private ProductEventListener productEventListener;

    @MockitoBean
    private ProductSearchService productSearchService;

    @MockitoBean
    private ProductSearchController productSearchController;

    // 테스트용 상품
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // 상품 생성 (실제 DB에 저장 - 동시성 테스트를 위해 필요)
        // 카테고리는 nullable이므로 null로 설정 가능
        testProduct = Product.builder()
                .name("테스트 상품")
                .category(null) // 카테고리는 선택적
                .sellerCode("SELLER-001")
                .sellerName("테스트 판매자")
                .quantity(5) // 초기 재고: 5개
                .description("동시성 테스트용 상품")
                .price(10000)
                .status(ProductStatus.ON_SALE)
                .isDeleted(false)
                .images(new java.util.ArrayList<>())
                .build();
        testProduct = productRepository.save(testProduct);
    }

    @DisplayName("동시성 테스트: 사용자 2명이 각각 3개씩 동시 주문 시도 (재고 5개)")
    @Test
    void concurrency() throws InterruptedException {
        // given: 재고 5개인 상품
        String productCode = testProduct.getCode();
        int initialQuantity = testProduct.getQuantity();
        assertThat(initialQuantity).isEqualTo(5);

        // 사용자 2명이 각각 3개씩 주문 시도 (총 6개 주문 시도, 재고는 5개)
        int orderQuantity = 3;
        int numberOfUsers = 2;
        
        // 동시성 테스트를 위한 준비
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfUsers);
        CountDownLatch startLatch = new CountDownLatch(1); // 모든 스레드가 동시에 시작하도록
        CountDownLatch finishLatch = new CountDownLatch(numberOfUsers); // 모든 스레드가 완료될 때까지 대기
        
        // 성공/실패 카운터
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<Exception> exceptions = new ArrayList<>();

        // when: 2명의 사용자가 동시에 재고 차감 시도
        for (int i = 0; i < numberOfUsers; i++) {
            final int userIndex = i + 1;
            executorService.submit(() -> {
                try {
                    // 모든 스레드가 준비될 때까지 대기
                    startLatch.await();
                    
                    log.info("사용자 {} - 재고 차감 시작: productCode={}, quantity={}", 
                            userIndex, productCode, orderQuantity);
                    
                    // 재고 차감 시도
                    productService.updateInventory(productCode, -orderQuantity);
                    
                    log.info("사용자 {} - 재고 차감 성공", userIndex);
                    successCount.incrementAndGet();
                    
                } catch (InsufficientInventoryException e) {
                    log.warn("사용자 {} - 재고 부족 예외 발생: {}", userIndex, e.getMessage());
                    failureCount.incrementAndGet();
                    exceptions.add(e);
                } catch (Exception e) {
                    log.error("사용자 {} - 예상치 못한 예외 발생", userIndex, e);
                    failureCount.incrementAndGet();
                    exceptions.add(e);
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // 모든 스레드가 준비되면 동시에 시작
        Thread.sleep(100); // 스레드 준비 시간
        startLatch.countDown();
        
        // 모든 스레드가 완료될 때까지 대기
        finishLatch.await();

        // then: 검증
        log.info("테스트 완료 - 성공: {}, 실패: {}", successCount.get(), failureCount.get());
        
        // 1. 성공한 주문은 1개만 있어야 함 (재고 5개 중 3개 차감)
        assertThat(successCount.get())
                .as("성공한 주문은 1개여야 함 (재고 5개 중 3개만 차감 가능)")
                .isEqualTo(1);
        
        // 2. 실패한 주문은 1개여야 함 (재고 부족)
        assertThat(failureCount.get())
                .as("실패한 주문은 1개여야 함 (재고 부족)")
                .isEqualTo(1);
        
        // 3. 실패한 경우 InsufficientInventoryException이 발생해야 함
        assertThat(exceptions)
                .as("실패한 경우 InsufficientInventoryException이 발생해야 함")
                .hasSize(1)
                .allMatch(e -> e instanceof InsufficientInventoryException);
        
        // 4. 최종 재고는 2개여야 함 (5개 - 3개 = 2개)
        Product finalProduct = productRepository.findByCodeAndIsDeletedFalse(productCode)
                .orElseThrow();
        int finalQuantity = finalProduct.getQuantity();
        
        assertThat(finalQuantity)
                .as("최종 재고는 2개여야 함 (초기 5개 - 차감 3개 = 2개)")
                .isEqualTo(2);
        
        log.info("최종 재고: {}개 (초기: {}개, 차감: {}개)", 
                finalQuantity, initialQuantity, orderQuantity);
        
        executorService.shutdown();
    }

}
