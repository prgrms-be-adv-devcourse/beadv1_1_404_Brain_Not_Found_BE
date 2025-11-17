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
                .sellerId(3L) // User 모듈의 seller ID
                .sellerName("판매자1")
                .quantity(100)
                .description("테스트용 상품입니다.")
                .price(10000)
                .status(ProductStatus.ON_SALE)
                .isDeleted(false)
                .build();
        productRepository.save(product1);
        log.info("상품 생성 완료: {} (code: {})", product1.getName(), product1.getCode());

        // 테스트용 상품 2 (판매중)
        Product product2 = Product.builder()
                .name("테스트 상품 2")
                .sellerId(3L)
                .sellerName("판매자1")
                .quantity(50)
                .description("테스트용 상품 2입니다.")
                .price(20000)
                .status(ProductStatus.ON_SALE)
                .isDeleted(false)
                .build();
        productRepository.save(product2);
        log.info("상품 생성 완료: {} (code: {})", product2.getName(), product2.getCode());

        // 테스트용 상품 3 (판매대기)
        Product product3 = Product.builder()
                .name("테스트 상품 3")
                .sellerId(3L)
                .sellerName("판매자1")
                .quantity(30)
                .description("판매 대기 중인 상품입니다.")
                .price(15000)
                .status(ProductStatus.WAITING)
                .isDeleted(false)
                .build();
        productRepository.save(product3);
        log.info("상품 생성 완료: {} (code: {})", product3.getName(), product3.getCode());

        // 테스트용 상품 4 (품절)
        Product product4 = Product.builder()
                .name("테스트 상품 4")
                .sellerId(3L)
                .sellerName("판매자1")
                .quantity(0)
                .description("품절된 상품입니다.")
                .price(30000)
                .status(ProductStatus.SOLD_OUT)
                .isDeleted(false)
                .build();
        productRepository.save(product4);
        log.info("상품 생성 완료: {} (code: {})", product4.getName(), product4.getCode());

        log.info("더미 상품 데이터 생성 완료. 총 {}개", productRepository.count());
    }
}

