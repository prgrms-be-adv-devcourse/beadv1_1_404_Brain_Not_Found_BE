package com.ll.products.config;

import com.ll.products.domain.product.model.entity.Product;
import com.ll.products.domain.product.model.entity.ProductStatus;
import com.ll.products.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!prod") // 프로덕션 환경에서는 실행되지 않도록
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            log.info("더미 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        log.info("더미 상품 데이터를 생성합니다...");

        // 테스트용 상품 1 (판매중)
        Product product1 = Product.builder()
                .name("테스트 상품 1")
                .sellerCode("USER-001")
                .sellerName("판매자1")
                .quantity(100)
                .description("테스트용 상품입니다.")
                .price(10000)
                .status(ProductStatus.ON_SALE)
                .isDeleted(false)
                .build();
        productRepository.save(product1);
        log.info("========================================");
        log.info("상품 1 생성 완료:");
        log.info("  - 이름: {}", product1.getName());
        log.info("  - 코드: {}", product1.getCode());
        log.info("  - ID: {}", product1.getId());
        log.info("  - 가격: {}원", product1.getPrice());
        log.info("  - 수량: {}", product1.getQuantity());
        log.info("  - 상태: {}", product1.getStatus());
        log.info("  - 판매자 코드: {}", product1.getSellerCode());
        log.info("========================================");

        // 테스트용 상품 2 (판매중)
        Product product2 = Product.builder()
                .name("테스트 상품 2")
                .sellerCode("USER-001")
                .sellerName("판매자1")
                .quantity(50)
                .description("테스트용 상품 2입니다.")
                .price(20000)
                .status(ProductStatus.ON_SALE)
                .isDeleted(false)
                .build();
        productRepository.save(product2);
        log.info("========================================");
        log.info("상품 2 생성 완료:");
        log.info("  - 이름: {}", product2.getName());
        log.info("  - 코드: {}", product2.getCode());
        log.info("  - ID: {}", product2.getId());
        log.info("  - 가격: {}원", product2.getPrice());
        log.info("  - 수량: {}", product2.getQuantity());
        log.info("  - 상태: {}", product2.getStatus());
        log.info("  - 판매자 코드: {}", product2.getSellerCode());
        log.info("========================================");

        // 테스트용 상품 3 (판매대기)
        Product product3 = Product.builder()
                .name("테스트 상품 3")
                .sellerCode("USER-002")
                .sellerName("판매자2")
                .quantity(30)
                .description("판매 대기 중인 상품입니다.")
                .price(15000)
                .status(ProductStatus.WAITING)
                .isDeleted(false)
                .build();
        productRepository.save(product3);
        log.info("========================================");
        log.info("상품 3 생성 완료:");
        log.info("  - 이름: {}", product3.getName());
        log.info("  - 코드: {}", product3.getCode());
        log.info("  - ID: {}", product3.getId());
        log.info("  - 가격: {}원", product3.getPrice());
        log.info("  - 수량: {}", product3.getQuantity());
        log.info("  - 상태: {}", product3.getStatus());
        log.info("  - 판매자 코드: {}", product3.getSellerCode());
        log.info("========================================");

        // 테스트용 상품 4 (품절)
        Product product4 = Product.builder()
                .name("테스트 상품 4")
                .sellerCode("USER-002")
                .sellerName("판매자2")
                .quantity(0)
                .description("품절된 상품입니다.")
                .price(30000)
                .status(ProductStatus.SOLD_OUT)
                .isDeleted(false)
                .build();
        productRepository.save(product4);
        log.info("========================================");
        log.info("상품 4 생성 완료:");
        log.info("  - 이름: {}", product4.getName());
        log.info("  - 코드: {}", product4.getCode());
        log.info("  - ID: {}", product4.getId());
        log.info("  - 가격: {}원", product4.getPrice());
        log.info("  - 수량: {}", product4.getQuantity());
        log.info("  - 상태: {}", product4.getStatus());
        log.info("  - 판매자 코드: {}", product4.getSellerCode());
        log.info("========================================");

        log.info("더미 상품 데이터 생성 완료. 총 {}개", productRepository.count());
        log.info("========================================");
        log.info("생성된 상품 코드 목록 (Postman 테스트 시 사용):");
        log.info("  1. {} - {}", product1.getCode(), product1.getName());
        log.info("  2. {} - {}", product2.getCode(), product2.getName());
        log.info("  3. {} - {}", product3.getCode(), product3.getName());
        log.info("  4. {} - {}", product4.getCode(), product4.getName());
        log.info("========================================");
    }
}

