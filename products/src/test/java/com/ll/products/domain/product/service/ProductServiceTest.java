package com.ll.products.domain.product.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.ll.products.domain.category.model.entity.Category;
import com.ll.products.domain.category.repository.CategoryRepository;
import com.ll.products.domain.product.model.dto.ProductImageDto;
import com.ll.products.domain.product.model.dto.request.ProductCreateRequest;
import com.ll.products.domain.product.model.dto.request.ProductUpdateRequest;
import com.ll.products.domain.product.model.dto.request.ProductUpdateStatusRequest;
import com.ll.products.domain.product.model.dto.response.ProductListResponse;
import com.ll.products.domain.product.model.dto.response.ProductResponse;
import com.ll.products.domain.product.model.entity.Product;
import com.ll.products.domain.product.model.entity.ProductStatus;
import com.ll.products.domain.product.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService 테스트")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Category testCategory;
    private Product testProduct;
    private ProductCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .name("전자제품")
                .build();

        testProduct = Product.builder()
                .name("맥북 프로")
                .category(testCategory)
                .sellerId(1L)
                .sellerName(null)
                .quantity(10)
                .description("M3 맥북 프로")
                .price(2500000)
                .status(ProductStatus.WAITING)
                .isDeleted(false)
                .images(new ArrayList<>())
                .build();

        List<ProductImageDto> images = List.of(
                ProductImageDto.builder()
                        .url("https://example.com/image1.jpg")
                        .sequence(0)
                        .isMain(true)
                        .build(),
                ProductImageDto.builder()
                        .url("https://example.com/image2.jpg")
                        .sequence(1)
                        .isMain(false)
                        .build()
        );

        createRequest = ProductCreateRequest.builder()
                .name("맥북 프로")
                .categoryId(1L)
                .sellerId(1L)
                .quantity(10)
                .description("M3 맥북 프로")
                .price(2500000)
                .images(images)
                .build();
    }

    @DisplayName("상품 생성")
    @Test
    void createProduct() {
        // given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // when
        ProductResponse result = productService.createProduct(createRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("맥북 프로");
        assertThat(result.categoryId()).isEqualTo(testCategory.getId());
        assertThat(result.sellerId()).isEqualTo(1L);
        assertThat(result.price()).isEqualTo(2500000);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        Product capturedProduct = productCaptor.getValue();

        assertThat(capturedProduct.getName()).isEqualTo("맥북 프로");
        assertThat(capturedProduct.getCategory()).isEqualTo(testCategory);
        assertThat(capturedProduct.getImages()).hasSize(2);
    }

    @DisplayName("상품 상세조회")
    @Test
    void getProduct() {
        // given
        when(productRepository.findByCodeAndIsDeletedFalse("PROD-001"))
                .thenReturn(Optional.of(testProduct));

        // when
        ProductResponse result = productService.getProduct("PROD-001");

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("맥북 프로");
        verify(productRepository).findByCodeAndIsDeletedFalse("PROD-001");
    }

    @DisplayName("상품 목록 조회")
    @Test
    void getProducts() {
        // given
        List<Product> products = List.of(testProduct);
        Page<Product> productPage = new PageImpl<>(products);
        Pageable pageable = PageRequest.of(0, 10);

        when(productRepository.searchProducts(1L, null, ProductStatus.WAITING, null, pageable))
                .thenReturn(productPage);

        // when
        Page<ProductListResponse> result = productService.getProducts(
                1L, null, ProductStatus.WAITING, null, pageable
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("맥북 프로");
        assertThat(result.getContent().get(0).sellerId()).isEqualTo(1L);
    }

    @DisplayName("상품 삭제")
    @Test
    void deleteProduct() {
        // given
        when(productRepository.findByCodeAndIsDeletedFalse("PROD-001"))
                .thenReturn(Optional.of(testProduct));

        // when
        productService.deleteProduct("PROD-001");

        // then
        assertThat(testProduct.getIsDeleted()).isTrue();
    }

    @DisplayName("상품 수정")
    @Test
    void updateProduct() {
        // given
        Category newCategory = Category.builder()
                .name("컴퓨터")
                .build();

        List<ProductImageDto> newImages = List.of(
                ProductImageDto.builder()
                        .url("https://example.com/new-image1.jpg")
                        .sequence(0)
                        .isMain(true)
                        .build(),
                ProductImageDto.builder()
                        .url("https://example.com/new-image2.jpg")
                        .sequence(1)
                        .isMain(false)
                        .build()
        );

        ProductUpdateRequest updateRequest = ProductUpdateRequest.builder()
                .name("맥북 프로 M4")
                .categoryId(2L)
                .quantity(20)
                .description("M4 맥북 프로")
                .price(3000000)
                .images(newImages)
                .build();

        when(productRepository.findByCodeAndIsDeletedFalse("PROD-001"))
                .thenReturn(Optional.of(testProduct));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));

        // when
        ProductResponse result = productService.updateProduct("PROD-001", updateRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(testProduct.getName()).isEqualTo("맥북 프로 M4");
        assertThat(testProduct.getQuantity()).isEqualTo(20);
        assertThat(testProduct.getPrice()).isEqualTo(3000000);
        assertThat(testProduct.getDescription()).isEqualTo("M4 맥북 프로");
        assertThat(testProduct.getCategory()).isEqualTo(newCategory);
        assertThat(testProduct.getImages()).hasSize(2);
    }

    @DisplayName("상품 상태 변경")
    @Test
    void updateProductStatus() {
        // given
        ProductUpdateStatusRequest statusRequest = new ProductUpdateStatusRequest(ProductStatus.ON_SALE);

        when(productRepository.findByCodeAndIsDeletedFalse("PROD-001"))
                .thenReturn(Optional.of(testProduct));

        // when
        ProductResponse result = productService.updateProductStatus("PROD-001", statusRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(testProduct.getStatus()).isEqualTo(ProductStatus.ON_SALE);
    }
}