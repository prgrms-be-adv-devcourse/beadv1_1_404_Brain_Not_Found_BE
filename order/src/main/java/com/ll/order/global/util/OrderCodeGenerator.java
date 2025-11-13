package com.ll.order.global.util;

import com.github.f4b6a3.uuid.UuidCreator;

public class OrderCodeGenerator { // TODO : Order 엔티티에서 할 수 있도록
    private static final String ORD = "ORD-";
    private static final String ORD_ITEM = "ORD-ITEM-";

    private OrderCodeGenerator() {}

    public static String newOrderCode() {
        return ORD + UuidCreator.getTimeOrderedEpoch()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    public static String newOrderItemCode() {
        return ORD_ITEM + UuidCreator.getTimeOrderedEpoch()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

}
