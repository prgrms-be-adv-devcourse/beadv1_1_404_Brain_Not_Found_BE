package com.ll.products.domain.search.service;

import com.ll.products.domain.search.document.ProductDocument;
import com.ll.products.domain.search.dto.ProductSearchResponse;
import com.ll.products.domain.search.repository.ProductSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductSearchService 테스트")
class ProductSearchServiceTest {

    @Mock
    private ProductSearchRepository productSearchRepository;

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @InjectMocks
    private ProductSearchService productSearchService;

    private ProductDocument testProduct1;
    private ProductDocument testProduct2;
    private ProductDocument testProduct3;

    @BeforeEach
    void setUp() {
        testProduct1 = ProductDocument.builder()
                .id(1L)
                .code("PROD-001")
                .name("에스파 닝닝 포토카드")
                .description("에스파 닝닝의 공식 포토카드입니다")
                .price(15000)
                .quantity(100)
                .sellerCode("1L")
                .sellerName("판매자1")
                .categoryId(1L)
                .categoryName("아이돌 굿즈")
                .status("APPROVED")
                .isDeleted(false)
                .mainImageUrl("https://example.com/image1.jpg")
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();

        testProduct2 = ProductDocument.builder()
                .id(2L)
                .code("PROD-002")
                .name("에스파 카리나 포토카드")
                .description("에스파 카리나의 공식 포토카드입니다")
                .price(20000)
                .quantity(50)
                .sellerCode("1L")
                .sellerName("판매자1")
                .categoryId(1L)
                .categoryName("아이돌 굿즈")
                .status("APPROVED")
                .isDeleted(false)
                .mainImageUrl("https://example.com/image2.jpg")
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now())
                .build();

        testProduct3 = ProductDocument.builder()
                .id(3L)
                .code("PROD-003")
                .name("BTS 정국 포토카드")
                .description("BTS 정국의 공식 포토카드입니다")
                .price(25000)
                .quantity(30)
                .sellerCode("2L")
                .sellerName("판매자2")
                .categoryId(2L)
                .categoryName("방탄소년단")
                .status("APPROVED")
                .isDeleted(false)
                .mainImageUrl("https://example.com/image3.jpg")
                .createdAt(LocalDateTime.now().minusDays(3))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("키워드로 상품 검색")
    void searchByKeyword_Success() {
        // given
        String keyword = "닝닝";
        Pageable pageable = PageRequest.of(0, 20);

        SearchHit<ProductDocument> searchHit = mock(SearchHit.class);
        when(searchHit.getContent()).thenReturn(testProduct1);

        SearchHits<ProductDocument> searchHits = mock(SearchHits.class);
        when(searchHits.getTotalHits()).thenReturn(1L);
        when(searchHits.getSearchHits()).thenReturn(List.of(searchHit));

        when(elasticsearchOperations.search(any(NativeQuery.class), eq(ProductDocument.class)))
                .thenReturn(searchHits);

        // when
        Page<ProductSearchResponse> result = productSearchService.search(keyword, null, null, null, null, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("에스파 닝닝 포토카드");
    }

    @Test
    @DisplayName("최신순 전체 조회")
    void searchWithoutKeyword_OrderByCreatedAtDesc() {
        // given
        Pageable pageable = PageRequest.of(0, 20);

        SearchHit<ProductDocument> hit1 = mock(SearchHit.class);
        SearchHit<ProductDocument> hit2 = mock(SearchHit.class);
        SearchHit<ProductDocument> hit3 = mock(SearchHit.class);

        when(hit1.getContent()).thenReturn(testProduct1);
        when(hit2.getContent()).thenReturn(testProduct2);
        when(hit3.getContent()).thenReturn(testProduct3);

        SearchHits<ProductDocument> searchHits = mock(SearchHits.class);
        when(searchHits.getTotalHits()).thenReturn(3L);
        when(searchHits.getSearchHits()).thenReturn(List.of(hit1, hit2, hit3));

        when(elasticsearchOperations.search(any(NativeQuery.class), eq(ProductDocument.class)))
                .thenReturn(searchHits);

        // when
        Page<ProductSearchResponse> result = productSearchService.search(null, null, null, null, null, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(3L);
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("가격 범위로 검색")
    void searchByPriceRange_Success() {
        // given
        Integer minPrice = 10000;
        Integer maxPrice = 20000;
        Pageable pageable = PageRequest.of(0, 20);

        SearchHit<ProductDocument> hit1 = mock(SearchHit.class);
        SearchHit<ProductDocument> hit2 = mock(SearchHit.class);

        when(hit1.getContent()).thenReturn(testProduct1);
        when(hit2.getContent()).thenReturn(testProduct2);

        SearchHits<ProductDocument> searchHits = mock(SearchHits.class);
        when(searchHits.getTotalHits()).thenReturn(2L);
        when(searchHits.getSearchHits()).thenReturn(List.of(hit1, hit2));

        when(elasticsearchOperations.search(any(NativeQuery.class), eq(ProductDocument.class)))
                .thenReturn(searchHits);

        // when
        Page<ProductSearchResponse> result = productSearchService.search(null, null, minPrice, maxPrice, null, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2L);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .allMatch(product -> product.price() >= minPrice && product.price() <= maxPrice);
    }

    @Test
    @DisplayName("상태 필터로 검색")
    void searchByStatus_Success() {
        // given
        String status = "APPROVED";
        Pageable pageable = PageRequest.of(0, 20);

        SearchHit<ProductDocument> hit1 = mock(SearchHit.class);
        SearchHit<ProductDocument> hit2 = mock(SearchHit.class);
        SearchHit<ProductDocument> hit3 = mock(SearchHit.class);

        when(hit1.getContent()).thenReturn(testProduct1);
        when(hit2.getContent()).thenReturn(testProduct2);
        when(hit3.getContent()).thenReturn(testProduct3);

        SearchHits<ProductDocument> searchHits = mock(SearchHits.class);
        when(searchHits.getTotalHits()).thenReturn(3L);
        when(searchHits.getSearchHits()).thenReturn(List.of(hit1, hit2, hit3));

        when(elasticsearchOperations.search(any(NativeQuery.class), eq(ProductDocument.class)))
                .thenReturn(searchHits);

        // when
        Page<ProductSearchResponse> result = productSearchService.search(null, null, null, null, status, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(3L);
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("키워드 + 카테고리 + 가격 범위")
    void searchWithMultipleFilters_Success() {
        // given
        String keyword = "에스파";
        Long categoryId = 1L;
        Integer minPrice = 10000;
        Integer maxPrice = 20000;
        Pageable pageable = PageRequest.of(0, 20);

        SearchHit<ProductDocument> hit1 = mock(SearchHit.class);
        SearchHit<ProductDocument> hit2 = mock(SearchHit.class);

        when(hit1.getContent()).thenReturn(testProduct1);
        when(hit2.getContent()).thenReturn(testProduct2);

        SearchHits<ProductDocument> searchHits = mock(SearchHits.class);
        when(searchHits.getTotalHits()).thenReturn(2L);
        when(searchHits.getSearchHits()).thenReturn(List.of(hit1, hit2));

        when(elasticsearchOperations.search(any(NativeQuery.class), eq(ProductDocument.class)))
                .thenReturn(searchHits);

        // when
        Page<ProductSearchResponse> result = productSearchService.search(keyword, categoryId, minPrice, maxPrice, null, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2L);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .allMatch(product ->
                    product.categoryName().equals("아이돌 굿즈") &&
                    product.price() >= minPrice &&
                    product.price() <= maxPrice
                );
    }

    @Test
    @DisplayName("검색 결과 없음")
    void searchNoResults_ReturnEmptyPage() {
        // given
        String keyword = "존재하지않는상품";
        Pageable pageable = PageRequest.of(0, 20);

        SearchHits<ProductDocument> searchHits = mock(SearchHits.class);
        when(searchHits.getTotalHits()).thenReturn(0L);
        when(searchHits.getSearchHits()).thenReturn(List.of());

        when(elasticsearchOperations.search(any(NativeQuery.class), eq(ProductDocument.class)))
                .thenReturn(searchHits);

        // when
        Page<ProductSearchResponse> result = productSearchService.search(keyword, null, null, null, null, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(0L);
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("페이징 처리")
    void searchWithPaging_SecondPage() {
        // given
        Pageable pageable = PageRequest.of(1, 2);

        SearchHit<ProductDocument> hit3 = mock(SearchHit.class);
        when(hit3.getContent()).thenReturn(testProduct3);

        SearchHits<ProductDocument> searchHits = mock(SearchHits.class);
        when(searchHits.getTotalHits()).thenReturn(3L);
        when(searchHits.getSearchHits()).thenReturn(List.of(hit3));

        when(elasticsearchOperations.search(any(NativeQuery.class), eq(ProductDocument.class)))
                .thenReturn(searchHits);

        // when
        Page<ProductSearchResponse> result = productSearchService.search(null, null, null, null, null, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(3L);
        assertThat(result.getNumber()).isEqualTo(1); // 2페이지 (0-based)
        assertThat(result.getTotalPages()).isEqualTo(2);
    }
}
