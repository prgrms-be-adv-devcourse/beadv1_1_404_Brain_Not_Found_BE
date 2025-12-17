package com.ll.products.domain.search.controller;
import com.ll.core.model.persistence.BaseEntity;
import com.ll.core.model.response.BaseResponse;
import com.ll.products.domain.history.service.HistoryFacadeService;
import com.ll.products.domain.recommendation.controller.swagger.ReindexAllProductsApiResponse;
import com.ll.products.domain.search.dto.ProductSearchResponse;
import com.ll.products.domain.search.service.ProductSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Search", description = "상품 검색 API")
@Slf4j
@RestController
@RequestMapping("/api/products/search")
@RequiredArgsConstructor
public class ProductSearchController {

    private final ProductSearchService productSearchService;
    private final HistoryFacadeService historyFacadeService;

    @Operation(
            summary = "상품 목록 검색"
    )
    @GetMapping()
    public ResponseEntity<BaseResponse<Page<ProductSearchResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) String status,
            @RequestHeader(value = "X-User-Code", required = false ) String userCode,
            @ParameterObject() @PageableDefault(size = 20) Pageable pageable
    ) {
        if(userCode != null){
            historyFacadeService.saveSearch(userCode,keyword);
        }
        Page<ProductSearchResponse> result = productSearchService.search(keyword, categoryId, minPrice, maxPrice, status, pageable);
        return BaseResponse.ok(result);
    }

    // 2. 전체 상품 재색인 (관리자용)
    @PostMapping("/reindex")
    @ReindexAllProductsApiResponse
    public ResponseEntity<BaseResponse<String>> reindexAllProducts(
            @RequestHeader("X-Role") String role
    ) {
        log.info("=== Elasticsearch 전체 재색인 시작 ===");
        productSearchService.reindexAll(role);
        log.info("=== Elasticsearch 전체 재색인 종료 ===");
        return BaseResponse.ok("재색인 완료");
    }
}