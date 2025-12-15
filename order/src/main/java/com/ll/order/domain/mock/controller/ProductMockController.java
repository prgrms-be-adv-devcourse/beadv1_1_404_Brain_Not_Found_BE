package com.ll.order.domain.mock.controller;

import com.ll.core.model.response.BaseResponse;
import com.ll.order.domain.client.ProductServiceClient;
import com.ll.order.domain.model.enums.product.ProductStatus;
import com.ll.order.domain.model.vo.response.product.ProductImageDto;
import com.ll.order.domain.model.vo.response.product.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductMockController {

    private final ProductServiceClient productServiceClient;

    @Value("${external.product-service.url:http://localhost:8085}")
    private String productServiceUrl;

    @GetMapping("/{productCode}")
    public ResponseEntity<BaseResponse<ProductResponse>> getProduct(
            @PathVariable String productCode
    ) {
        log.info("Mock Product Service - 상품 조회 요청: productCode={}", productCode);

        // PROD-001에 대한 더미 데이터 반환
        if ("PROD-001".equals(productCode)) {
            ProductResponse productResponse = ProductResponse.builder()
                    .id(1L)
                    .code("PROD-001")
                    .name("테스트 상품")
                    .categoryId(1L)
                    .categoryName("테스트 카테고리")
                    .sellerCode("SELLER-001")
                    .sellerName("테스트 판매자")
                    .status(ProductStatus.ON_SALE)
                    .quantity(100) // 재고 100개
                    .description("테스트용 더미 상품입니다.")
                    .price(10000)
                    .images(List.of(
                            ProductImageDto.builder()
                                    .url("https://example.com/images/product001.jpg")
                                    .sequence(0)
                                    .isMain(true)
                                    .build()
                    ))
                    .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0, 0))
                    .updatedAt(LocalDateTime.of(2024, 1, 1, 0, 0, 0))
                    .build();

            log.info("Mock Product Service - 상품 조회 성공: productCode={}", productCode);
            return BaseResponse.ok(productResponse);
        }

        // 존재하지 않는 상품
        log.warn("Mock Product Service - 상품을 찾을 수 없음: productCode={}", productCode);
        return BaseResponse.error(com.ll.core.model.exception.ErrorCode.NOT_FOUND);
    }

    @PatchMapping("/{productCode}/inventory")
    public ResponseEntity<Void> decreaseInventory(
            @PathVariable String productCode,
            @RequestBody Map<String, Integer> request
    ) {
        Integer quantity = request.get("quantity");
        log.info("========== Mock Product Service - 재고 차감 요청 ==========");
        log.info("productCode: {}, quantity: {}", productCode, quantity);
        log.info("실제 Product 서비스 호출 시작 (락 쿼리 실행 확인)");
        log.info("Product 서비스 URL: {}", productServiceUrl);

        try {
            // 실제 ProductServiceClient를 통해 실제 Product 서비스 호출 -> 락 쿼리 적용
            productServiceClient.decreaseInventory(productCode, quantity);
            
            log.info("========== Mock Product Service - 재고 차감 성공 ==========");
            log.info("productCode: {}, quantity: {}", productCode, quantity);
            log.info("실제 Product 서비스에서 락 쿼리 실행 완료");
            log.info("ProductRepository.findByCodeWithLock() -> PESSIMISTIC_WRITE 락 적용됨");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("========== Mock Product Service - 재고 차감 실패 ==========");
            log.error("productCode: {}, quantity: {}", productCode, quantity);
            log.error("실제 Product 서비스 호출 실패: {}", e.getMessage(), e);
            throw e;
        }
    }
}

