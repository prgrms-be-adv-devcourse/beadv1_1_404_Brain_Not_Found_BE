package com.ll.products.domain.product.controller;

import com.ll.core.model.response.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/ping")
    public ResponseEntity<BaseResponse<String>> pong() {
        System.out.println("ProductController.pong");
        return BaseResponse.ok("Ok");
    }


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

    // 2. 상품 상세조회(code 기반)
    @GetMapping("/{code}")
    public ResponseEntity<BaseResponse<ProductResponse>> getProduct(@PathVariable String code) {
        ProductResponse response = productService.getProduct(code);
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
}
