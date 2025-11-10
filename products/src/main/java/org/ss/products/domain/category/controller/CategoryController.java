package org.ss.products.domain.category.controller;

import com.example.core.model.response.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ss.products.domain.category.model.dto.request.CategoryCreateRequest;
import org.ss.products.domain.category.model.dto.request.CategoryUpdateRequest;
import org.ss.products.domain.category.model.dto.response.CategoryResponse;
import org.ss.products.domain.category.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // 카테고리 생성
    @PostMapping
    public ResponseEntity<BaseResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return BaseResponse.created(response);
    }

    // 카테고리 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<CategoryResponse>> getCategory(@PathVariable Long id) {
        CategoryResponse response = categoryService.getCategory(id);
        return BaseResponse.ok(response);
    }

    // 최상위 카테고리 목록 조회
    @GetMapping("/root")
    public ResponseEntity<BaseResponse<List<CategoryResponse>>> getRootCategories() {
        List<CategoryResponse> response = categoryService.getRootCategories();
        return BaseResponse.ok(response);
    }

    // 카테고리 전체 조회 (flat)
    @GetMapping
    public ResponseEntity<BaseResponse<List<CategoryResponse>>> getAllCategoriesFlat() {
        List<CategoryResponse> response = categoryService.getAllCategoriesFlat();
        return BaseResponse.ok(response);
    }

    // 카테고리 전체 조회(tree)
    @GetMapping("/tree")
    public ResponseEntity<BaseResponse<List<CategoryResponse>>> getAllCategoriesTree() {
        List<CategoryResponse> response = categoryService.getAllCategoriesTree();
        return BaseResponse.ok(response);
    }

    // 카테고리 수정
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request) {
        CategoryResponse response = categoryService.updateCategory(id, request);
        return BaseResponse.ok(response);
    }

    // 카테고리 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return BaseResponse.ok(null);
    }
}
