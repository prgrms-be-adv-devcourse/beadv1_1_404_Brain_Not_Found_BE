package com.ll.order.domain.model.entity;

import com.ll.core.model.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 양방향 관계 테스트를 위한 OrderItem 엔티티
 * 실제 프로덕션에서는 사용하지 않고 성능 비교 테스트용으로만 사용
 */
@Entity
@Getter
@Table(name = "order_items_bidirectional")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemWithBidirectional extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderWithBidirectional order;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String sellerCode;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer price;

    private OrderItemWithBidirectional(OrderWithBidirectional order,
                                        Long productId,
                                        String sellerCode,
                                        String productName,
                                        Integer quantity,
                                        Integer price) {
        this.order = order;
        this.productId = productId;
        this.sellerCode = sellerCode;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public static OrderItemWithBidirectional create(OrderWithBidirectional order,
                                                    Long productId,
                                                    String sellerCode,
                                                    String productName,
                                                    Integer quantity,
                                                    Integer price) {
        OrderItemWithBidirectional orderItem = new OrderItemWithBidirectional(
                order, productId, sellerCode, productName, quantity, price);
        // 양방향 관계 설정
        order.getOrderItems().add(orderItem);
        return orderItem;
    }
}

