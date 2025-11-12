package com.ll.order.domain.model.enums;

public enum ProductSaleStatus {
    ON_SALE,
    SOLD_OUT,
    STOP_SALE,
    DELETED;

    public boolean isSaleable() {
        return this == ON_SALE;
    }
}

