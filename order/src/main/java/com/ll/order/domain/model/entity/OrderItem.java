package com.ll.order.domain.model.entity;

import com.ll.core.model.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "order_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

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
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer price;

    private OrderItem(Order order,
                      Long productId,
                      Long sellerId,
                      String orderItemCode,
                      String productName,
                      Integer quantity,
                      Integer price) {
        this.order = order;
        this.productId = productId;
        this.sellerId = sellerId;
        this.orderItemCode = orderItemCode;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public static OrderItem create(Order order,
                                   Long productId,
                                   Long sellerId,
                                   String orderItemCode,
                                   String productName,
                                   Integer quantity,
                                   Integer price) {
        return new OrderItem(order, productId, sellerId, orderItemCode, productName, quantity, price);
    }
}

