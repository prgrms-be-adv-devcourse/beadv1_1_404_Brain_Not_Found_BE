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
import com.ll.products.domain.product.model.entity.Product;
import com.ll.products.domain.product.model.entity.ProductStatus;
import com.ll.products.domain.product.repository.ProductRepository;
import com.ll.products.global.client.UserClient;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserClient userClient;
    private final ApplicationEventPublisher eventPublisher;

    // 1. 상품 생성
    @Transactional
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
        log.info("상품 생성 완료: {} (ID: {})", savedProduct.getName(), savedProduct.getId());
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
        log.info("상품 삭제 완료: {} (ID: {})", product.getName(), product.getId());
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
        log.info("상품 수정 완료: {} (ID: {})", product.getName(), product.getId());
        eventPublisher.publishEvent(ProductEvent.updated(this, product));
        return ProductResponse.from(product);
    }

    // 6. 상품 상태변경
    @Transactional
    public ProductResponse updateProductStatus(String code, ProductUpdateStatusRequest request, String userCode, String role) {
        Product product = getProductByCode(code);
        validateOwnership(product, userCode, role);
        product.updateStatus(request.status());
        log.info("상품 상태변경 완료: {} -> {} (ID: {})", product.getName(), request.status(), product.getId());
        eventPublisher.publishEvent(ProductEvent.updated(this, product));
        return ProductResponse.from(product);
    }

    // 7. 재고 수정
    @Transactional
    public void updateInventory(String code, Integer quantity) {
        Product product = getProductByCode(code);
        product.updateQuantity(quantity);
        log.info("재고 수정 완료: {}, 남은재고: {}", product.getName(), product.getQuantity());
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
