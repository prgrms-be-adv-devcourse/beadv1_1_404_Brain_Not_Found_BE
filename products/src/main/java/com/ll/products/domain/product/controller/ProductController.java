package com.ll.products.domain.product.controller;

import com.ll.core.model.response.BaseResponse;
import com.ll.products.domain.history.service.HistoryFacadeService;
import com.ll.products.domain.product.model.dto.request.ProductUpdateInventoryRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ll.products.domain.product.model.dto.request.ProductCreateRequest;
import com.ll.products.domain.product.model.dto.request.ProductUpdateStatusRequest;
import com.ll.products.domain.product.model.dto.request.ProductUpdateRequest;
import com.ll.products.domain.product.model.dto.response.ProductListResponse;
import com.ll.products.domain.product.model.dto.response.ProductResponse;
import com.ll.products.domain.product.model.entity.ProductStatus;
import com.ll.products.domain.product.service.ProductService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    // 락 + 재고 HOLD는 “최종 결제 버튼 누른 후 서버에 진짜 구매 요청이 도착한 순간"
    // 재고 변동은 반드시 동기(Sync) -> 비관적 락
    // 결제/후처리는 비동기 가능 -> 사용자 경험 개선 ( 오래 걸림 )

    private final ProductService productService;
    private final HistoryFacadeService historyFacadeService;

    // 1. 상품 생성
    @PostMapping
    public ResponseEntity<BaseResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            @RequestHeader("X-User-Code") String sellerCode,
            @RequestHeader("X-Role") String role
    ) {
        ProductResponse response = productService.createProduct(request, sellerCode, role);
        return BaseResponse.created(response);
    }

    // 2. 상품 상세조회
    @GetMapping("/{code}")
    public ResponseEntity<BaseResponse<ProductResponse>> getProduct(
            @PathVariable String code,
            @RequestHeader(value = "X-User-Code",required = false) String userCode) {

        ProductResponse response = productService.getProduct(code);
        if(userCode != null){
            historyFacadeService.saveView(userCode,code);
            log.info("getProduct Controller 접근 usercode: {}",userCode);
        }
        return BaseResponse.ok(response);
    }

    // 3. 상품 목록조회
    @GetMapping
    public ResponseEntity<BaseResponse<Page<ProductListResponse>>> getProducts(
            @RequestParam(required = false) String sellerCode,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) String name,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ProductListResponse> response = productService.getProducts(
                sellerCode, categoryId, status, name, pageable
        );
        return BaseResponse.ok(response);
    }

    // 4. 상품 삭제(soft delete)
    @DeleteMapping("/{code}")
    public ResponseEntity<BaseResponse<Void>> deleteProduct(
            @PathVariable String code,
            @RequestHeader("X-User-Code") String userCode,
            @RequestHeader("X-Role") String role
    ) {
        productService.deleteProduct(code, userCode, role);
        return BaseResponse.ok(null);
    }

    // 5. 상품 수정
    @PutMapping("/{code}")
    public ResponseEntity<BaseResponse<ProductResponse>> updateProduct(
            @PathVariable String code,
            @Valid @RequestBody ProductUpdateRequest request,
            @RequestHeader("X-User-Code") String userCode,
            @RequestHeader("X-Role") String role
    ) {
        ProductResponse response = productService.updateProduct(code, request, userCode, role);
        return BaseResponse.ok(response);
    }

    // 6. 상품 상태변경
    @PatchMapping("/{code}/status")
    public ResponseEntity<BaseResponse<ProductResponse>> updateProductStatus(
            @PathVariable String code,
            @Valid @RequestBody ProductUpdateStatusRequest request,
            @RequestHeader("X-User-Code") String userCode,
            @RequestHeader("X-Role") String role
    ) {
        ProductResponse response = productService.updateProductStatus(code, request, userCode, role);
        return BaseResponse.ok(response);
    }

    // 7. 재고 변동 ( 증가/감소 )
    @PatchMapping("/{code}/inventory")
    public ResponseEntity<BaseResponse<Void>> updateInventory(
            @PathVariable String code,
            @Valid @RequestBody ProductUpdateInventoryRequest request
    ) {
        productService.updateInventory(code, request.quantity());
        return BaseResponse.ok(null);
    }
}
