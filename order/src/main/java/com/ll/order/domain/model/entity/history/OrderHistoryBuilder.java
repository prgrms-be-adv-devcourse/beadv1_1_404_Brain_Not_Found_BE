package com.ll.order.domain.model.entity.history;

import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.entity.OrderItem;
import com.ll.order.domain.model.enums.order.OrderHistoryActionType;
import com.ll.order.domain.model.enums.order.OrderStatus;

import java.util.List;

/**
 * 주문 이력 생성을 위한 Builder 패턴 구현
 * OrderHistoryEntity.create() 호출의 복잡성을 캡슐화하고 가독성을 향상시킴
 */
public class OrderHistoryBuilder {
    private Order order;
    private List<OrderItem> orderItems;
    private OrderHistoryActionType actionType;
    private OrderStatus previousStatus;
    private String reason;
    private String errorMessage;
    private String requestData;
    private String responseData;
    private String createdBy;

    private OrderHistoryBuilder() {
        // 기본값 설정
        this.requestData = null;
        this.responseData = null;
        this.createdBy = "SYSTEM";
    }

    public static OrderHistoryBuilder builder() {
        return new OrderHistoryBuilder();
    }

    public OrderHistoryBuilder order(Order order) {
        this.order = order;
        return this;
    }

    public OrderHistoryBuilder orderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
        return this;
    }

    public OrderHistoryBuilder actionType(OrderHistoryActionType actionType) {
        this.actionType = actionType;
        return this;
    }

    public OrderHistoryBuilder previousStatus(OrderStatus previousStatus) {
        this.previousStatus = previousStatus;
        return this;
    }

    public OrderHistoryBuilder reason(String reason) {
        this.reason = reason;
        return this;
    }

    public OrderHistoryBuilder errorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public OrderHistoryBuilder requestData(String requestData) {
        this.requestData = requestData;
        return this;
    }

    public OrderHistoryBuilder responseData(String responseData) {
        this.responseData = responseData;
        return this;
    }

    public OrderHistoryBuilder createdBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public OrderHistoryEntity build() {
        return OrderHistoryEntity.create(
                order,
                orderItems,
                actionType,
                previousStatus,
                reason,
                errorMessage,
                requestData,
                responseData,
                createdBy
        );
    }

    // 편의 메서드: 주문 생성 이력
    public static OrderHistoryEntity createOrderHistory(Order order, List<OrderItem> orderItems) {
        return builder()
                .order(order)
                .orderItems(orderItems)
                .actionType(OrderHistoryActionType.CREATE)
                .previousStatus(null)
                .reason("주문 생성")
                .createdBy("SYSTEM")
                .build();
    }

    // 편의 메서드: 결제 성공 이력
    public static OrderHistoryEntity createPaymentSuccessHistory(
            Order order, List<OrderItem> orderItems, OrderStatus previousStatus, String paymentType) {
        return builder()
                .order(order)
                .orderItems(orderItems)
                .actionType(OrderHistoryActionType.STATUS_CHANGE)
                .previousStatus(previousStatus)
                .reason(paymentType + " 결제 완료")
                .createdBy("SYSTEM")
                .build();
    }

    // 편의 메서드: 결제 실패 이력
    public static OrderHistoryEntity createPaymentFailHistory(
            Order order, List<OrderItem> orderItems, OrderStatus previousStatus, String paymentType, String errorMessage) {
        return builder()
                .order(order)
                .orderItems(orderItems)
                .actionType(OrderHistoryActionType.STATUS_CHANGE)
                .previousStatus(previousStatus)
                .reason(paymentType + " 결제 실패")
                .errorMessage(errorMessage)
                .createdBy("SYSTEM")
                .build();
    }

    // 편의 메서드: 주문 상태 변경 이력
    public static OrderHistoryEntity createStatusChangeHistory(
            Order order, List<OrderItem> orderItems, OrderStatus previousStatus, String reason, String createdBy) {
        return builder()
                .order(order)
                .orderItems(orderItems)
                .actionType(OrderHistoryActionType.STATUS_CHANGE)
                .previousStatus(previousStatus)
                .reason(reason)
                .createdBy(createdBy)
                .build();
    }
}

