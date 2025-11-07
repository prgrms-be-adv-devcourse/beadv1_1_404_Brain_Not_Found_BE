package com.ll.order.domain.model.entity;

import com.ll.order.global.baseEntity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false, unique = true)
    private String orderItemCode;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer price;

    @Builder
    public OrderItem(Order order, Long productId, Long sellerId, String orderItemCode, Integer quantity, Integer price) {
        this.order = order;
        this.productId = productId;
        this.sellerId = sellerId;
        this.orderItemCode = orderItemCode;
        this.quantity = quantity;
        this.price = price;
    }
    
    public void setOrder(Order order) {
        this.order = order;
    }
}

