package com.ll.products.domain.category.service;

import com.ll.products.domain.category.exception.CategoryPermissionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ll.products.domain.category.model.dto.request.CategoryCreateRequest;
import com.ll.products.domain.category.model.dto.request.CategoryUpdateRequest;
import com.ll.products.domain.category.model.dto.response.CategoryResponse;
import com.ll.products.domain.category.model.entity.Category;
import com.ll.products.domain.category.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService 테스트")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;
    private Category testParentCategory;

    @BeforeEach
    void setUp() {
        testParentCategory = Category.builder()
                .name("전자제품")
                .build();

        testCategory = Category.builder()
                .name("스마트폰")
                .build();
    }

    @DisplayName("1. 카테고리 생성 성공 - ADMIN 권한")
    @Test
    void createCategory() {
        // given
        String role = "ADMIN";
        CategoryCreateRequest request = new CategoryCreateRequest("스마트폰", 1L);
        Category parentCategory = Category.builder()
                .name("전자제품")
                .build();

        Category savedCategory = Category.builder()
                .name("스마트폰")
                .build();
        savedCategory.setParent(parentCategory);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        // when
        CategoryResponse result = categoryService.createCategory(request, role);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("스마트폰");

        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(categoryCaptor.capture());
        Category capturedCategory = categoryCaptor.getValue();

        assertThat(capturedCategory.getName()).isEqualTo("스마트폰");
        assertThat(capturedCategory.getParent()).isEqualTo(parentCategory);
        assertThat(capturedCategory.getDepth()).isEqualTo(2);
    }

    @DisplayName("1-1. 카테고리 생성 실패 - 권한 없음")
    @Test
    void createCategoryFailNoPermission() {
        // given
        String role = "USER";
        CategoryCreateRequest request = new CategoryCreateRequest("스마트폰", 1L);

        // when & then
        assertThatThrownBy(() -> categoryService.createCategory(request, role))
                .isInstanceOf(CategoryPermissionException.class);

        verify(categoryRepository, never()).save(any());
    }

    @DisplayName("카테고리 상세조회")
    @Test
    void getCategory() {
        // given
        Category category = Category.builder()
                .name("전자제품")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // when
        CategoryResponse result = categoryService.getCategory(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("전자제품");

        verify(categoryRepository).findById(1L);
    }

    @DisplayName("Root 카테고리 목록 조회")
    @Test
    void getRootCategories() {
        // given
        List<Category> rootCategories = new ArrayList<>();
        rootCategories.add(Category.builder().name("전자제품").build());
        rootCategories.add(Category.builder().name("의류").build());
        rootCategories.add(Category.builder().name("식품").build());

        when(categoryRepository.findByParentIsNull()).thenReturn(rootCategories);

        // when
        List<CategoryResponse> result = categoryService.getRootCategories();

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(CategoryResponse::getName)
                .containsExactly("전자제품", "의류", "식품");

        verify(categoryRepository).findByParentIsNull();
    }

    @DisplayName("전체 카테고리 조회")
    @Test
    void getAllCategoriesFlat() {
        // given
        List<Category> allCategories = new ArrayList<>();
        allCategories.add(Category.builder().name("전자제품").build());
        allCategories.add(Category.builder().name("스마트폰").build());
        allCategories.add(Category.builder().name("노트북").build());

        when(categoryRepository.findAll()).thenReturn(allCategories);

        // when
        List<CategoryResponse> result = categoryService.getAllCategoriesFlat();

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(CategoryResponse::getName)
                .containsExactly("전자제품", "스마트폰", "노트북");

        verify(categoryRepository).findAll();
    }

    @DisplayName("4. 카테고리명 수정 성공 - ADMIN 권한")
    @Test
    void updateCategoryName() {
        // given
        String role = "ADMIN";
        Category category = Category.builder()
                .name("전자제품")
                .build();

        CategoryUpdateRequest request = new CategoryUpdateRequest("전자기기", null);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // when
        CategoryResponse result = categoryService.updateCategory(1L, request, role);

        // then
        assertThat(result).isNotNull();

        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(categoryCaptor.capture());
        Category capturedCategory = categoryCaptor.getValue();

        assertThat(capturedCategory.getName()).isEqualTo("전자기기");
    }

    @DisplayName("4-1. 카테고리 수정 실패 - 권한 없음")
    @Test
    void updateCategoryFailNoPermission() {
        // given
        String role = "SELLER";
        CategoryUpdateRequest request = new CategoryUpdateRequest("전자기기", null);

        // when & then
        assertThatThrownBy(() -> categoryService.updateCategory(1L, request, role))
                .isInstanceOf(CategoryPermissionException.class);

        verify(categoryRepository, never()).findById(any());
        verify(categoryRepository, never()).save(any());
    }

    @DisplayName("5. 부모 카테고리 변경 성공")
    @Test
    void updateParentCategory() {
        // given
        String role = "ADMIN";
        Category oldParent = Category.builder()
                .name("전자제품")
                .build();

        Category category = Category.builder()
                .name("스마트폰")
                .build();
        category.setParent(oldParent);

        Category newParent = Category.builder()
                .name("모바일 기기")
                .build();

        CategoryUpdateRequest request = new CategoryUpdateRequest("스마트폰", 2L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newParent));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // when
        CategoryResponse result = categoryService.updateCategory(1L, request, role);

        // then
        assertThat(result).isNotNull();

        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(categoryCaptor.capture());
        Category capturedCategory = categoryCaptor.getValue();

        assertThat(capturedCategory.getParent()).isEqualTo(newParent);
    }

    @DisplayName("6. 카테고리 삭제 성공 - ADMIN 권한")
    @Test
    void deleteCategory() {
        // given
        String role = "ADMIN";
        Category category = Category.builder()
                .name("전자제품")
                .children(new ArrayList<>())
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // when
        categoryService.deleteCategory(1L, role);

        // then
        verify(categoryRepository).delete(category);
    }

    @DisplayName("6-1. 카테고리 삭제 실패 - 권한 없음")
    @Test
    void deleteCategoryFailNoPermission() {
        // given
        String role = "USER";

        // when & then
        assertThatThrownBy(() -> categoryService.deleteCategory(1L, role))
                .isInstanceOf(CategoryPermissionException.class);

        verify(categoryRepository, never()).findById(any());
        verify(categoryRepository, never()).delete(any());
    }
}