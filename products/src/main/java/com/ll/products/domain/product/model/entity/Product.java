package com.ll.products.domain.product.model.entity;

import com.ll.core.model.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.ll.products.domain.category.model.entity.Category;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private String sellerCode;

    @Column(nullable = false)
    private String sellerName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProductStatus status = ProductStatus.WAITING;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    // 기본 정보 변경
    public void updateBasicInfo(String name, String description, Integer price, Integer quantity) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (price != null) {
            this.price = price;
        }
        if (quantity != null) {
            this.quantity = quantity;
        }
    }

    public void updateCategory(Category category) {
        this.category = category;
    }

    public void updateStatus(ProductStatus status) {
        this.status = status;
    }

    public void updateQuantity(Integer quantity) {
        this.quantity += quantity;
    }

    public void softDelete() {
        this.isDeleted = true;
    }

    public void addImage(ProductImage image) {
        this.images.add(image);
        image.updateProduct(this);
    }

    public void deleteImages() {
        this.images.clear();
    }
}