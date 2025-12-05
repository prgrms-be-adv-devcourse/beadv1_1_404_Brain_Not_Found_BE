package com.ll.products.domain.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ll.products.domain.category.exception.CategoryNotFoundException;
import com.ll.products.domain.category.model.dto.request.CategoryCreateRequest;
import com.ll.products.domain.category.model.dto.request.CategoryUpdateRequest;
import com.ll.products.domain.category.model.dto.response.CategoryResponse;
import com.ll.products.domain.category.model.entity.Category;
import com.ll.products.domain.category.repository.CategoryRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // 카테고리 생성
    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        Category category = Category.builder()
                .name(request.getName())
                .build();
        setParentCategory(request, category);
        Category savedCategory = categoryRepository.save(category);
        log.info("카테고리 생성 완료: {}", savedCategory.getId());
        return convertToResponse(savedCategory, false);
    }

    // 카테고리 상세조회(자식 카테고리 1 depth 조회)
    public CategoryResponse getCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        return convertToResponse(category, true);
    }

    // root 카테고리 조회
    public List<CategoryResponse> getRootCategories() {
        return categoryRepository.findByParentIsNull().stream()
                .map(category -> convertToResponse(category, false))
                .toList();
    }

    // 카테고리 전체 조회(flat)
    public List<CategoryResponse> getAllCategoriesFlat() {
        return categoryRepository.findAll().stream()
                .map(category -> convertToResponse(category, false))
                .toList();
    }

    // 카테고리 전체 조회(tree)
    public List<CategoryResponse> getAllCategoriesTree() {
        List<Category> allCategories = categoryRepository.findAll();

        // parentId를 키로 하는 Map 생성
        Map<Long, List<Category>> childrenMap = allCategories.stream()
                .filter(category -> category.getParent() != null)
                .collect(Collectors.groupingBy(category -> category.getParent().getId()));

        // root 카테고리 조회하여 트리 구성
        return allCategories.stream()
                .filter(category -> category.getParent() == null)
                .map(category -> buildCategoryTree(category, childrenMap))
                .toList();
    }

    // 트리 구성 및 dto 전환
    private CategoryResponse buildCategoryTree(Category category, Map<Long, List<Category>> childrenMap) {
        List<CategoryResponse> children = childrenMap.getOrDefault(category.getId(), List.of()).stream()
                .map(child -> buildCategoryTree(child, childrenMap))
                .toList();
        return CategoryResponse.builder()
                .id(category.getId())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .name(category.getName())
                .depth(category.getDepth())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .children(children.isEmpty() ? null : children)
                .build();
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        category.updateName(request.getName());

        // 부모 카테고리 변경 시
        if (request.getParentId() != null) {
            if (!request.getParentId().equals(category.getParent() != null ? category.getParent().getId() : null)) {
                Category newParent = categoryRepository.findById(request.getParentId())
                        .orElseThrow(() -> new CategoryNotFoundException(request.getParentId()));

                // 순환참조 체크
                if (isCircularReference(category, newParent)) {
                    throw new IllegalStateException("자신의 자식 카테고리를 부모 카테고리로 설정할 수 없습니다.");
                }

                category.removeParent();
                category.setParent(newParent);
            }
        } else if (category.getParent() != null) {
            category.removeParent();
        }

        Category updatedCategory = categoryRepository.save(category);
        log.info("카테고리 수정완료: {}", updatedCategory.getId());
        return convertToResponse(updatedCategory, false);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        if (!category.getChildren().isEmpty()) {
            throw new IllegalStateException("하위 카테고리가 있는 카테고리는 삭제할 수 없습니다.");
        }

        categoryRepository.delete(category);
        log.info("카테고리 삭제완료: {}", id);
    }




    // 순환참조 확인 메서드
    private boolean isCircularReference(Category category, Category newParent) {
        Category current = newParent;
        while (current != null) {
            if (current.equals(category)) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    // 부모 카테고리 등록 메서드
    private void setParentCategory(CategoryCreateRequest request, Category category) {
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.getParentId()));
            category.setParent(parent);
        }
    }

    // entity -> dto
    private CategoryResponse convertToResponse(Category category, boolean includeChildren) {
        return CategoryResponse.builder()
                .id(category.getId())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .name(category.getName())
                .depth(category.getDepth())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .children(includeChildren ? category.getChildren().stream()
                                .map(child -> convertToResponse(child, false))
                                .toList()
                        : null)
                .build();
    }


}
