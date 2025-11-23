package com.ll.order.domain.model.entity;

import com.ll.core.model.persistence.BaseEntity;
import com.ll.order.domain.model.enums.OrderStatus;
import com.ll.order.domain.model.enums.OrderType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

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

    public static Order create(Long buyerId, OrderType orderType, String address) {
        Order order = new Order();
        order.buyerId = buyerId;
        order.orderType = orderType;
        order.orderStatus = OrderStatus.CREATED;
        order.address = address;
        order.totalPrice = 0;
        return order;
    }

    public OrderItem createOrderItem(Long productId,
                                     String productCode,
                                     String sellerCode,
                                     String productName,
                                     int quantity,
                                     int pricePerUnit) {
        OrderItem orderItem = OrderItem.create(
                this,
                productId,
                productCode,
                sellerCode,
                productName,
                quantity,
                pricePerUnit
        );
        increaseTotalPrice(pricePerUnit * quantity);
        return orderItem;
    }

    public void changeStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    private void increaseTotalPrice(int amount) {
        this.totalPrice += amount;
    }

}
