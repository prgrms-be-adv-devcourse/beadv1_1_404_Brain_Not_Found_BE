package com.ll.order.domain.model.enums;

public enum OrderStatus {
    CREATED,
    CANCELLED,
    PAID,
    DELIVERY,
    COMPLETED;

    public boolean canTransitionTo(OrderStatus target) {
        return switch (this) {
            case CREATED -> target == PAID || target == CANCELLED;
            case PAID -> target == DELIVERY || target == CANCELLED;
            case DELIVERY -> target == COMPLETED;
            case CANCELLED, COMPLETED -> false;
        };
    }
}
