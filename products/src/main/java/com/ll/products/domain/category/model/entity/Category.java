package com.ll.products.domain.category.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_categories")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Category {

    public static final int MAX_DEPTH = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Category> children = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Integer depth = 1;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 부모 관계 적용
    public void setParent(Category parent) {
        if (parent != null) {
            if (parent.getDepth() >= MAX_DEPTH) {
                throw new IllegalStateException("카테고리 깊이는 최대 " + MAX_DEPTH + "까지만 생성할 수 있습니다.");
            }
            this.parent = parent;
            this.depth = parent.getDepth() + 1;
            parent.getChildren().add(this);
        } else {
            this.parent = null;
            this.depth = 1;
        }
    }

    // 이름 수정
    public void updateName(String name){
        this.name = name;
    }

    // 부모 관계 삭제
    public void removeParent() {
        if (this.parent != null) {
            this.parent.getChildren().remove(this);
            this.parent = null;
            this.depth = 1;
        }
    }
}

