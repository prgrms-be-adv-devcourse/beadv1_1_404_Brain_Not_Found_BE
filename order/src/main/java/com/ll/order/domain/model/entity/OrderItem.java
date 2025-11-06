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

    @Column(nullable = false)
    private Long orderId;

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
    public OrderItem(Long orderId, Long productId, Long sellerId, String orderItemCode, Integer quantity, Integer price) {
        this.orderId = orderId;
        this.productId = productId;
        this.sellerId = sellerId;
        this.orderItemCode = orderItemCode;
        this.quantity = quantity;
        this.price = price;
    }
}

