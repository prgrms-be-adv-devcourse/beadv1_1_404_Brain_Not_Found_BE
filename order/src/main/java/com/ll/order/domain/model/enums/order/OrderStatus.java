package com.ll.order.domain.model.enums.order;

public enum OrderStatus {
    CREATED,
    CANCELLED,
    PAID,
    DELIVERY,
    COMPLETED,
    REFUNDED,
    FAILED;

    public boolean canTransitionTo(OrderStatus target) {
        return switch (this) {
            case CREATED -> target == PAID || target == CANCELLED;
            case PAID -> target == DELIVERY || target == CANCELLED || target == REFUNDED;
            case DELIVERY -> target == COMPLETED || target == REFUNDED;
            case COMPLETED -> target == REFUNDED;
            case CANCELLED, REFUNDED -> false;
            case FAILED -> false;
        };
    }
}
