package com.ll.products.domain.product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ll.products.domain.category.exception.CategoryNotFoundException;
import com.ll.products.domain.category.model.entity.Category;
import com.ll.products.domain.category.repository.CategoryRepository;
import com.ll.products.domain.product.exception.ProductNotFoundException;
import com.ll.products.domain.product.model.dto.ProductImageDto;
import com.ll.products.domain.product.model.dto.request.ProductCreateRequest;
import com.ll.products.domain.product.model.dto.request.ProductUpdateStatusRequest;
import com.ll.products.domain.product.model.dto.request.ProductUpdateRequest;
import com.ll.products.domain.product.model.dto.response.ProductListResponse;
import com.ll.products.domain.product.model.dto.response.ProductResponse;
import com.ll.products.domain.product.model.entity.Product;
import com.ll.products.domain.product.model.entity.ProductStatus;
import com.ll.products.domain.product.repository.ProductRepository;
import com.ll.products.domain.search.document.ProductDocument;
import com.ll.products.domain.search.repository.ProductSearchRepository;
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
    private final ProductSearchRepository productSearchRepository;

    // 1. 상품 생성
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        Category category = getCategory(request.categoryId());
        String sellerName = getSellerName(request.sellerId());
        Product product = Product.builder()
                .name(request.name())
                .category(category)
                .sellerId(request.sellerId())
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

        // Elasticsearch 동기화
        syncToElasticsearch(savedProduct);

        return ProductResponse.from(savedProduct);
    }

    // 2. 상품 상세조회(code 기반)
    public ProductResponse getProduct(String code) {
        Product product = getProductByCode(code);
        return ProductResponse.from(product);
    }

    // 3. 상품 목록조회
    public Page<ProductListResponse> getProducts(
            Long sellerId,
            Long categoryId,
            ProductStatus status,
            String name,
            Pageable pageable
    ) {
        Page<Product> products = productRepository.searchProducts(sellerId, categoryId, status, name, pageable);
        return products.map(ProductListResponse::from);
    }

    // 4. 상품 삭제(soft delete)
    @Transactional
    public void deleteProduct(String code) {
        Product product = getProductByCode(code);
        product.softDelete();
        log.info("상품 삭제 완료: {} (ID: {})", product.getName(), product.getId());

        // Elasticsearch 동기화 (삭제된 상품도 동기화하여 isDeleted 상태 반영)
        syncToElasticsearch(product);
    }

    // 5. 상품 수정
    @Transactional
    public ProductResponse updateProduct(String code, ProductUpdateRequest request) {
        Product product = getProductByCode(code);
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

        // Elasticsearch 동기화
        syncToElasticsearch(product);

        return ProductResponse.from(product);
    }

    // 6. 상품 상태변경
    @Transactional
    public ProductResponse updateProductStatus(String code, ProductUpdateStatusRequest request) {
        Product product = getProductByCode(code);
        product.updateStatus(request.status());
        log.info("상품 상태변경 완료: {} -> {} (ID: {})", product.getName(), request.status(), product.getId());

        // Elasticsearch 동기화
        syncToElasticsearch(product);

        return ProductResponse.from(product);
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

    // 판매자명 조회 TODO. User API 호출
    private String getSellerName(Long sellerId) {
        return "API 호출 전 임시 데이터";
//        return userClient.getSellerName(sellerId);
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

    /**
     * Elasticsearch 동기화
     * - Product Entity를 ProductDocument로 변환하여 Elasticsearch에 저장
     * - 생성/수정/삭제 모두 save로 처리 (upsert)
     */
    private void syncToElasticsearch(Product product) {
        try {
            ProductDocument document = ProductDocument.from(product);
            productSearchRepository.save(document);
            log.debug("Elasticsearch 동기화 완료: Product ID={}", product.getId());
        } catch (Exception e) {
            // Elasticsearch 동기화 실패 시 로그만 남기고 트랜잭션은 롤백하지 않음
            // (검색 기능이 중단되더라도 핵심 기능은 동작해야 함)
            log.error("Elasticsearch 동기화 실패: Product ID={}, error={}", product.getId(), e.getMessage(), e);
        }
    }
}
