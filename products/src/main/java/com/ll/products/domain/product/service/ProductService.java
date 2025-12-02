package com.ll.products.domain.product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ll.products.domain.category.exception.CategoryNotFoundException;
import com.ll.products.domain.category.model.entity.Category;
import com.ll.products.domain.category.repository.CategoryRepository;
import com.ll.products.domain.product.event.ProductEvent;
import com.ll.products.domain.product.exception.ProductNotFoundException;
import com.ll.products.domain.product.exception.ProductOwnershipException;
import com.ll.products.domain.product.model.dto.ProductImageDto;
import com.ll.products.domain.product.model.dto.request.ProductCreateRequest;
import com.ll.products.domain.product.model.dto.request.ProductUpdateStatusRequest;
import com.ll.products.domain.product.model.dto.request.ProductUpdateRequest;
import com.ll.products.domain.product.model.dto.response.ProductListResponse;
import com.ll.products.domain.product.model.dto.response.ProductResponse;
import com.ll.products.domain.product.model.entity.InventoryHistory;
import com.ll.products.domain.product.model.entity.InventoryHistoryStatus;
import com.ll.products.domain.product.model.entity.Product;
import com.ll.products.domain.product.model.entity.ProductStatus;
import com.ll.products.domain.product.repository.InventoryHistoryRepository;
import com.ll.products.domain.product.repository.ProductRepository;
import com.ll.products.global.client.UserClient;
import com.ll.core.model.vo.kafka.enums.InventoryEventType;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    // TODO : 모든 재고 차감 성공 확인 / 차감 실패 시 결제 취소 / 재고 차감 성공 "후" 주문 완료 <- 이 부분 처리 필요

    private final UserClient userClient;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;

    private final ApplicationEventPublisher eventPublisher;

    // 1. 상품 생성
    public ProductResponse createProduct(ProductCreateRequest request, String sellerCode, String role) {
        validateRole(role);
        Category category = getCategory(request.categoryId());
        String sellerName = getSellerName(sellerCode);
        Product product = Product.builder()
                .name(request.name())
                .category(category)
                .sellerCode(sellerCode)
                .sellerName(sellerName)
                .quantity(request.quantity())
                .description(request.description())
                .price(request.price())
                .status(ProductStatus.WAITING)
                .isDeleted(false)
                .build();
        setImages(request.images(), product);
        Product savedProduct = productRepository.save(product);
        log.debug("상품 생성 완료: {} (ID: {})", savedProduct.getName(), savedProduct.getId());
        eventPublisher.publishEvent(ProductEvent.created(this, savedProduct));
        return ProductResponse.from(savedProduct);
    }

    // 2. 상품 상세조회(code 기반)
    public ProductResponse getProduct(String code) {
        Product product = getProductByCode(code);
        return ProductResponse.from(product);
    }

    // 3. 상품 목록조회
    public Page<ProductListResponse> getProducts(
            String sellerCode,
            Long categoryId,
            ProductStatus status,
            String name,
            Pageable pageable
    ) {
        Page<Product> products = productRepository.searchProducts(sellerCode, categoryId, status, name, pageable);
        return products.map(ProductListResponse::from);
    }

    // 4. 상품 삭제(soft delete)
    @Transactional
    public void deleteProduct(String code, String userCode, String role) {
        Product product = getProductByCode(code);
        validateOwnership(product, userCode, role);
        product.softDelete();
        log.debug("상품 삭제 완료: {} (ID: {})", product.getName(), product.getId());
        eventPublisher.publishEvent(ProductEvent.deleted(this, product));
    }

    // 5. 상품 수정
    @Transactional
    public ProductResponse updateProduct(String code, ProductUpdateRequest request, String userCode, String role) {
        Product product = getProductByCode(code);
        validateOwnership(product, userCode, role);
        product.updateBasicInfo(
                request.name(),
                request.description(),
                request.price(),
                request.quantity()
        );
        Category category = getCategory(request.categoryId());
        product.updateCategory(category);
        setImages(request.images(), product);
        log.debug("상품 수정 완료: {} (ID: {})", product.getName(), product.getId());
        eventPublisher.publishEvent(ProductEvent.updated(this, product));
        return ProductResponse.from(product);
    }

    // 6. 상품 상태변경
    @Transactional
    public ProductResponse updateProductStatus(String code, ProductUpdateStatusRequest request, String userCode, String role) {
        Product product = getProductByCode(code);
        validateOwnership(product, userCode, role);
        product.updateStatus(request.status());
        log.debug("상품 상태변경 완료: {} -> {} (ID: {})", product.getName(), request.status(), product.getId());
        eventPublisher.publishEvent(ProductEvent.updated(this, product));
        return ProductResponse.from(product);
    }

    // 7. 재고 수정
    @Transactional
    public void updateInventory(String code, Integer quantity) { // 기존 api에서는 referenceCode가 없음
        updateInventory(code, quantity, null);
    }

    // 7-1. 재고 수정 (referenceCode 포함 - Kafka 이벤트에서 사용)
    @Transactional
    public void updateInventory(String code, Integer quantity, String referenceCode) {
        if (referenceCode != null && !referenceCode.isBlank()) { // 이벤트 수신된게 이력으로 남아있으면 (중복 수신이면)
            Optional<InventoryHistory> existing = inventoryHistoryRepository.findByReferenceCode(referenceCode);

            if (existing.isPresent()) {
                log.warn("이미 처리된 이벤트 - referenceCode: {}, productCode: {}, quantity: {}, 기존 처리 시간: {}",
                        referenceCode, code, quantity, existing.get().getProcessedAt());
                return;
            }
        }

        Product product = productRepository.findByCodeWithLock(code)
                .orElseThrow(() -> new ProductNotFoundException(code));

        // 재고 변동 전 수량 저장
        Integer beforeQuantity = product.getQuantity();

        // 재고 수정
        product.updateQuantity(quantity);

        // 재고 변동 후 수량 저장
        Integer afterQuantity = product.getQuantity();

        InventoryEventType transactionType = quantity < 0 ?
                InventoryEventType.STOCK_DECREMENT : InventoryEventType.STOCK_ROLLBACK;

        InventoryHistory history = InventoryHistory.builder()
                .productCode(code)
                .quantity(quantity)
                .transactionType(transactionType)
                .status(InventoryHistoryStatus.SUCCESS)
                .beforeQuantity(beforeQuantity)
                .afterQuantity(afterQuantity)
                .build();
        if (referenceCode != null && !referenceCode.isBlank()) { // 이벤트 수신으로 referenceCode가 있으면 set
            history.setReferenceCode(referenceCode);
        }

        history.markAsSuccess(beforeQuantity, afterQuantity);
        inventoryHistoryRepository.save(history);

        log.debug("재고 수정 완료: {}, 변경량: {}, 남은재고: {}",
                product.getName(), quantity, afterQuantity);
        log.debug("재고 이력 저장 완료 - id: {}, productCode: {}, beforeQuantity: {}, afterQuantity: {}",
                history.getId(), code, beforeQuantity, afterQuantity);
        eventPublisher.publishEvent(ProductEvent.updated(this, product));
    }

    // 카테고리 조회
    private Category getCategory(Long categoryId) {
        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CategoryNotFoundException(categoryId));
        }
        return category;
    }

    // 판매자명 조회
    private String getSellerName(String sellerCode) {
        String sellerName = userClient.getSellerName(sellerCode);
        return sellerName != null ? sellerName : "알 수 없는 판매자";
    }

    // 이미지 설정
    private static void setImages(List<ProductImageDto> imageDtoList, Product product) {
        product.deleteImages();
        if (imageDtoList != null) {
            imageDtoList.forEach(imageDto ->
                    product.addImage(imageDto.toEntity())
            );
        }
    }

    // 상품 조회(code 기반)
    private Product getProductByCode(String code) {
        Product product = productRepository.findByCodeAndIsDeletedFalse(code)
                .orElseThrow(() -> new ProductNotFoundException(code));
        return product;
    }

    // Role 검증
    private void validateRole(String role) {
        if (!"SELLER".equals(role) && !"ADMIN".equals(role)) {
            throw new ProductOwnershipException(null, "상품 생성 권한 없음");
        }
    }

    // 소유권 검증
    private void validateOwnership(Product product, String userCode, String role) {
        if ("ADMIN".equals(role)) {
            return;
        }
        if (!userCode.equals(product.getSellerCode())) {
            throw new ProductOwnershipException(null, product.getCode());
        }
    }
}
