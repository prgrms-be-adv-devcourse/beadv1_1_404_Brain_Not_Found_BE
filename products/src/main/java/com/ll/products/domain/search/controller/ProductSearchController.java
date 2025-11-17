package com.ll.products.domain.search.controller;
import com.ll.core.model.response.BaseResponse;
import com.ll.products.domain.search.dto.ProductSearchResponse;
import com.ll.products.domain.search.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    @GetMapping("/search")
    public ResponseEntity<BaseResponse<Page<ProductSearchResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        log.info("상품 검색 API 호출: keyword={}, categoryId={}, price={}-{}, status={}, pageable={}",
                keyword, categoryId, minPrice, maxPrice, status, pageable);
        Page<ProductSearchResponse> result = productSearchService.search(keyword, categoryId, minPrice, maxPrice, status, pageable);
        log.info("검색 결과: totalElements={}, totalPages={}, currentPage={}, size={}",
                result.getTotalElements(), result.getTotalPages(), result.getNumber(), result.getSize());
        return BaseResponse.ok(result);
    }
}