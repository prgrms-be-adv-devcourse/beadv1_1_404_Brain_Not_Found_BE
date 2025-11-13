package com.ll.order.domain.model.entity;

import com.example.core.model.persistence.BaseEntity;
import com.github.f4b6a3.uuid.UuidCreator;
import com.ll.order.domain.model.enums.OrderStatus;
import com.ll.order.domain.model.enums.OrderType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {
    private static final String ORDER_PREFIX = "ORD-";
    private static final String ORDER_ITEM_PREFIX = "ORD-ITEM-";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderCode;

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
        return create(generateOrderCode(), buyerId, orderType, address);
    }

    public static Order create(String orderCode, Long buyerId, OrderType orderType, String address) {
        Order order = new Order();
        order.orderCode = orderCode;
        order.buyerId = buyerId;
        order.orderType = orderType;
        order.orderStatus = OrderStatus.CREATED;
        order.address = address;
        order.totalPrice = 0;
        return order;
    }

    public OrderItem createOrderItem(Long productId,
                                     Long sellerId,
                                     String productName,
                                     int quantity,
                                     int pricePerUnit) {
        OrderItem orderItem = OrderItem.create(
                this,
                productId,
                sellerId,
                generateOrderItemCode(),
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

    private static String generateOrderCode() {
        return ORDER_PREFIX + UuidCreator.getTimeOrderedEpoch()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    private static String generateOrderItemCode() {
        return ORDER_ITEM_PREFIX + UuidCreator.getTimeOrderedEpoch()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}
