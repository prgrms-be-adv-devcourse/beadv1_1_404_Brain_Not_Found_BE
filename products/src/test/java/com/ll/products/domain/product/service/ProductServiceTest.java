package com.ll.products.domain.product.service;

import com.ll.products.domain.category.exception.CategoryNotFoundException;
import com.ll.products.domain.category.model.entity.Category;
import com.ll.products.domain.category.repository.CategoryRepository;
import com.ll.products.domain.product.exception.ProductNotFoundException;
import com.ll.products.domain.product.exception.ProductOwnershipException;
import com.ll.products.domain.product.model.dto.ProductImageDto;
import com.ll.products.domain.product.model.dto.request.ProductCreateRequest;
import com.ll.products.domain.product.model.dto.request.ProductUpdateStatusRequest;
import com.ll.products.domain.product.model.dto.response.ProductListResponse;
import com.ll.products.domain.product.model.dto.response.ProductResponse;
import com.ll.products.domain.product.model.entity.Product;
import com.ll.products.domain.product.model.entity.ProductStatus;
import com.ll.products.domain.product.repository.InventoryHistoryRepository;
import com.ll.products.domain.product.repository.ProductRepository;
import com.ll.products.domain.s3.service.S3Service;
import com.ll.products.global.client.UserClient;
import com.ll.products.global.exception.ProductAuthException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService 테스트")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private InventoryHistoryRepository inventoryHistoryRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ProductService productService;

    private Category testCategory;
    private Product testProduct;
    private ProductCreateRequest createRequest;
    private List<ProductImageDto> testImages;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(productService, "s3BaseUrl", "https://test-s3.amazonaws.com");

        testCategory = Category.builder()
                .name("전자제품")
                .build();
        ReflectionTestUtils.setField(testCategory, "id", 1L);
        testProduct = Product.builder()
                .name("맥북 프로")
                .category(testCategory)
                .sellerCode("USER-001")
                .sellerName("테스트 판매자")
                .quantity(10)
                .description("M3 맥북 프로")
                .price(2500000)
                .status(ProductStatus.WAITING)
                .isDeleted(false)
                .images(new ArrayList<>())
                .build();
        ReflectionTestUtils.setField(testProduct, "id", 1L);
        ReflectionTestUtils.setField(testProduct, "code", "PROD-001");
        testImages = List.of(
                ProductImageDto.builder()
                        .fileKey("image1.jpg")
                        .sequence(0)
                        .isMain(true)
                        .build(),
                ProductImageDto.builder()
                        .fileKey("image2.jpg")
                        .sequence(1)
                        .isMain(false)
                        .build()
        );
        createRequest = ProductCreateRequest.builder()
                .name("맥북 프로")
                .categoryId(1L)
                .quantity(10)
                .description("M3 맥북 프로")
                .price(2500000)
                .images(testImages)
                .build();
    }

    @Test
    @DisplayName("1. 상품 생성 성공")
    void createProductSuccess_Seller() {
        // given
        String sellerCode = "USER-001";
        String role = "SELLER";
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(userClient.getSellerName(sellerCode)).thenReturn("테스트 판매자");
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // when
        ProductResponse result = productService.createProduct(createRequest, sellerCode, role);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("맥북 프로");
        assertThat(result.sellerCode()).isEqualTo(sellerCode);
        assertThat(result.price()).isEqualTo(2500000);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        verify(eventPublisher).publishEvent(any());

        Product capturedProduct = productCaptor.getValue();
        assertThat(capturedProduct.getName()).isEqualTo("맥북 프로");
        assertThat(capturedProduct.getStatus()).isEqualTo(ProductStatus.WAITING);
    }

    @Test
    @DisplayName("1-2. 상품 생성 실패 (권한 없음)")
    void createProductFail_InvalidRole() {
        // given
        String sellerCode = "USER-001";
        String role = "BUYER";

        // when
        // then
        assertThatThrownBy(() -> productService.createProduct(createRequest, sellerCode, role))
                .isInstanceOf(ProductAuthException.class);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("1-3. 상품 생성 실패 (카테고리 없음)")
    void createProductFail_CategoryNotFound() {
        // given
        String sellerCode = "USER-001";
        String role = "SELLER";
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> productService.createProduct(createRequest, sellerCode, role))
                .isInstanceOf(CategoryNotFoundException.class);

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("2. 상품 상세 조회 성공")
    void getProductSuccess() {
        // given
        when(productRepository.findByCodeAndIsDeletedFalse("PROD-001"))
                .thenReturn(Optional.of(testProduct));

        // when
        ProductResponse result = productService.getProduct("PROD-001");

        // then
        assertThat(result).isNotNull();
        assertThat(result.code()).isEqualTo("PROD-001");
        assertThat(result.name()).isEqualTo("맥북 프로");
        verify(productRepository).findByCodeAndIsDeletedFalse("PROD-001");
    }

    @Test
    @DisplayName("2-1. 상품 조회 실패 (존재하지 않는 코드)")
    void getProductFail_NotFound() {
        // given
        when(productRepository.findByCodeAndIsDeletedFalse("INVALID-CODE"))
                .thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> productService.getProduct("INVALID-CODE"))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("3. 상품 목록 조회 성공")
    void getProductsSuccess() {
        // given
        List<Product> products = List.of(testProduct);
        Page<Product> productPage = new PageImpl<>(products);
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.searchProducts("USER-001", 1L, ProductStatus.WAITING, null, pageable))
                .thenReturn(productPage);

        // when
        Page<ProductListResponse> result = productService.getProducts(
                "USER-001", 1L, ProductStatus.WAITING, null, pageable
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("맥북 프로");
        assertThat(result.getContent().get(0).sellerCode()).isEqualTo("USER-001");
    }

    @Test
    @DisplayName("4. 상품 삭제 성공")
    void deleteProductSuccess_Owner() {
        // given
        String userCode = "USER-001";
        String role = "SELLER";
        when(productRepository.findByCodeAndIsDeletedFalse("PROD-001"))
                .thenReturn(Optional.of(testProduct));

        // when
        productService.deleteProduct("PROD-001", userCode, role);

        // then
        assertThat(testProduct.getIsDeleted()).isTrue();
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    @DisplayName("4-1. 상품 삭제 실패 (권한)")
    void deleteProductFail_NoOwnership() {
        // given
        String userCode = "USER-002";
        String role = "SELLER";
        when(productRepository.findByCodeAndIsDeletedFalse("PROD-001"))
                .thenReturn(Optional.of(testProduct));

        // when
        // then
        assertThatThrownBy(() -> productService.deleteProduct("PROD-001", userCode, role))
                .isInstanceOf(ProductAuthException.class);

        assertThat(testProduct.getIsDeleted()).isFalse();
    }

    @Test
    @DisplayName("5. 상품 상태 변경 성공")
    void updateProductStatusSuccess() {
        // given
        String userCode = "USER-001";
        String role = "SELLER";
        ProductUpdateStatusRequest statusRequest = new ProductUpdateStatusRequest(ProductStatus.ON_SALE);

        when(productRepository.findByCodeAndIsDeletedFalse("PROD-001"))
                .thenReturn(Optional.of(testProduct));

        // when
        ProductResponse result = productService.updateProductStatus("PROD-001", statusRequest, userCode, role);

        // then
        assertThat(result).isNotNull();
        assertThat(testProduct.getStatus()).isEqualTo(ProductStatus.ON_SALE);
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    @DisplayName("5-1. 상품 상태 변경 실패")
    void updateProductStatusFail_NoOwnership() {
        // given
        String userCode = "USER-002";
        String role = "SELLER";
        ProductUpdateStatusRequest statusRequest = new ProductUpdateStatusRequest(ProductStatus.ON_SALE);

        when(productRepository.findByCodeAndIsDeletedFalse("PROD-001"))
                .thenReturn(Optional.of(testProduct));

        // when
        // then
        assertThatThrownBy(() -> productService.updateProductStatus("PROD-001", statusRequest, userCode, role))
                .isInstanceOf(ProductAuthException.class);

        assertThat(testProduct.getStatus()).isEqualTo(ProductStatus.WAITING);
    }

    @Test
    @DisplayName("6. 재고 수정 성공")
    void updateInventorySuccess_Decrease() {
        // given
        String code = "PROD-001";
        Integer quantity = -3;
        when(productRepository.findByCodeWithLock(code))
                .thenReturn(Optional.of(testProduct));
        when(inventoryHistoryRepository.save(any())).thenReturn(null);

        // when
        productService.updateInventory(code, quantity);

        // then
        assertThat(testProduct.getQuantity()).isEqualTo(7);
        verify(inventoryHistoryRepository).save(any());
    }

    @Test
    @DisplayName("6-1. 재고 수정 실패 (존재하지 않는 상품)")
    void updateInventoryFail_ProductNotFound() {
        // given
        String code = "INVALID-CODE";
        Integer quantity = 5;
        when(productRepository.findByCodeWithLock(code))
                .thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> productService.updateInventory(code, quantity))
                .isInstanceOf(ProductNotFoundException.class);
        verify(inventoryHistoryRepository, never()).save(any());
    }
}