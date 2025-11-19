package com.ll.order.domain.model.entity;

import com.ll.core.model.persistence.BaseEntity;
import com.ll.order.domain.model.enums.OrderStatus;
import com.ll.order.domain.model.enums.OrderType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 양방향 관계 테스트를 위한 Order 엔티티
 * 실제 프로덕션에서는 사용하지 않고 성능 비교 테스트용으로만 사용
 */
@Entity
@Getter
@Table(name = "orders_bidirectional")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderWithBidirectional extends BaseEntity {

    @Column(nullable = false)
    private Long buyerId;

    @Column(nullable = false)
    private Integer totalPrice = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Column(nullable = false)
    private String address;

    // 양방향 관계: @OneToMany 추가
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<OrderItemWithBidirectional> orderItems = new ArrayList<>();

    public static OrderWithBidirectional create(Long buyerId, OrderType orderType, String address) {
        OrderWithBidirectional order = new OrderWithBidirectional();
        order.buyerId = buyerId;
        order.orderType = orderType;
        order.orderStatus = OrderStatus.CREATED;
        order.address = address;
        order.totalPrice = 0;
        return order;
    }

    public void changeStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    private void increaseTotalPrice(int amount) {
        this.totalPrice += amount;
    }
}

