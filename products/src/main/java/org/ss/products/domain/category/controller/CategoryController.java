package org.ss.products.domain.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 카테고리 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable Long id) {
        CategoryResponse response = categoryService.getCategory(id);
        return ResponseEntity.ok(response);
    }

    // 최상위 카테고리 목록 조회
    @GetMapping("/root")
    public ResponseEntity<List<CategoryResponse>> getRootCategories() {
        List<CategoryResponse> response = categoryService.getRootCategories();
        return ResponseEntity.ok(response);
    }

    // 카테고리 전체 조회 (flat)
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategoriesFlat() {
        List<CategoryResponse> response = categoryService.getAllCategoriesFlat();
        return ResponseEntity.ok(response);
    }

    // 카테고리 전체 조회(tree)
    @GetMapping("/tree")
    public ResponseEntity<List<CategoryResponse>> getAllCategoriesTree() {
        List<CategoryResponse> response = categoryService.getAllCategoriesTree();
        return ResponseEntity.ok(response);
    }

    // 카테고리 수정
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request) {
        CategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(response);
    }

    // 카테고리 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
