package com.ll.products.domain.product.service;

import com.ll.products.domain.product.exception.ImageUploadLimitException;
import com.ll.products.domain.product.exception.ProductImageNotFoundException;
import com.ll.products.domain.product.model.entity.ProductImage;
import com.ll.products.domain.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
@RequiredArgsConstructor
public class ProductService {
    // TODO : 모든 재고 차감 성공 확인 / 차감 실패 시 결제 취소 / 재고 차감 성공 "후" 주문 완료 <- 이 부분 처리 필요

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserClient userClient;
    private final ApplicationEventPublisher eventPublisher;
    private final S3Service s3Service;

    @Value("${cloud.aws.s3.base-url}")
    private String s3BaseUrl;

    private static final int MAX_IMAGE_COUNT = 5;

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
        log.info("상품 생성 완료: {} (ID: {})", savedProduct.getName(), savedProduct.getId());
        eventPublisher.publishEvent(ProductEvent.created(this, savedProduct));
        return ProductResponse.from(savedProduct, s3BaseUrl);
    }

    // 2. 상품 상세조회(code 기반)
    public ProductResponse getProduct(String code) {
        Product product = getProductByCode(code);
        return ProductResponse.from(product, s3BaseUrl);
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
        return products.map(product -> ProductListResponse.from(product, s3BaseUrl));
    }

    // 4. 상품 삭제(soft delete)
    @Transactional
    public void deleteProduct(String code, String userCode, String role) {
        Product product = getProductByCode(code);
        validateOwnership(product, userCode, role);
        List<String> fileKeys = product.getImages().stream().map(ProductImage::getFileKey).toList();
        deleteProductImagesFromS3(fileKeys);
        product.softDelete();
        log.info("상품 삭제 완료: {} (ID: {})", product.getName(), product.getId());
        eventPublisher.publishEvent(ProductEvent.deleted(this, product));
    }

    // 5. 상품 수정
    @Transactional
    public ProductResponse updateProduct(String code, ProductUpdateRequest request, String userCode, String role) {
        Product product = getProductByCode(code);
        validateImageSize(request, product);
        validateOwnership(product, userCode, role);
        deleteImages(request, product);
        setImages(request.addImages(), product);
        product.updateBasicInfo(
                request.name(),
                request.description(),
                request.price(),
                request.quantity()
        );
        Category category = getCategory(request.categoryId());
        product.updateCategory(category);
        log.info("상품 수정 완료: {} (ID: {})", product.getName(), product.getId());
        eventPublisher.publishEvent(ProductEvent.updated(this, product));
        return ProductResponse.from(product, s3BaseUrl);
    }

    // 6. 상품 상태변경
    @Transactional
    public ProductResponse updateProductStatus(String code, ProductUpdateStatusRequest request, String userCode, String role) {
        Product product = getProductByCode(code);
        validateOwnership(product, userCode, role);
        product.updateStatus(request.status());
        log.info("상품 상태변경 완료: {} -> {} (ID: {})", product.getName(), request.status(), product.getId());
        eventPublisher.publishEvent(ProductEvent.updated(this, product));
        return ProductResponse.from(product, s3BaseUrl);
    }

    // 7. 재고 수정
    @Transactional
    public void updateInventory(String code, Integer quantity) {
        // 비관적 락으로 상품 조회 (재고 차감 시 Race Condition 방지)
        Product product = productRepository.findByCodeWithLock(code)
                .orElseThrow(() -> new ProductNotFoundException(code));
        
        product.updateQuantity(quantity);
        log.info("재고 수정 완료: {}, 변경량: {}, 남은재고: {}", 
                product.getName(), quantity, product.getQuantity());
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

    // 이미지 추가
    private static void setImages(List<ProductImageDto> imageDtoList, Product product) {
        if (imageDtoList != null && !imageDtoList.isEmpty()) {
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

    // 상품 소유권 검증
    private void validateOwnership(Product product, String userCode, String role) {
        if ("ADMIN".equals(role)) {
            return;
        }
        if (!userCode.equals(product.getSellerCode())) {
            throw new ProductOwnershipException(null, product.getCode());
        }
    }

    // 이미지 삭제
    private void deleteImages(ProductUpdateRequest request, Product product) {
        if (request.deleteImageKeys() != null && !request.deleteImageKeys().isEmpty()) {
            List<ProductImage> imagesToDelete = product.getImages().stream()
                    .filter(image -> request.deleteImageKeys().contains(image.getFileKey()))
                    .toList();
            validateImageOwnership(request, imagesToDelete);
            deleteProductImagesFromS3(request.deleteImageKeys());
            product.deleteImages(imagesToDelete);
        }
    }

    // 이미지 소유권 검증
    private static void validateImageOwnership(ProductUpdateRequest request, List<ProductImage> imagesToDelete) {
        if (imagesToDelete.size() != request.deleteImageKeys().size()) {
            List<String> existKeys = imagesToDelete.stream()
                    .map(ProductImage::getFileKey)
                    .toList();
            List<String> invalidKeys = request.deleteImageKeys().stream()
                    .filter(key -> !existKeys.contains(key))
                    .toList();
            throw new ProductImageNotFoundException(invalidKeys.get(0));
        }
    }

    // S3에서 이미지 삭제
    private void deleteProductImagesFromS3(List<String> fileKeys) {
        if (fileKeys == null || fileKeys.isEmpty()) {
            return;
        }
        fileKeys.forEach(fileKey -> {
            try {
                s3Service.deleteImage(fileKey);
                log.info("S3 이미지 삭제 완료: {}", fileKey);
            } catch (Exception e) {
                log.warn("S3 이미지 삭제 실패: {}", fileKey, e);
            }
        });
    }

    // 이미지 수량 검증
    private static void validateImageSize(ProductUpdateRequest request, Product product) {
        if (product.getImages().size() - request.deleteImageKeys().size() + request.addImages().size() > MAX_IMAGE_COUNT) {
            throw new ImageUploadLimitException(MAX_IMAGE_COUNT);
        }
    }
}
