package com.ll.order.domain.model.vo.response.order;

import java.math.BigDecimal;
import java.util.List;

public record OrderValidateResponse(
        String buyerCode,
        long totalQuantity,
        BigDecimal totalAmount,
        List<ItemInfo> items
) {
    public static OrderValidateResponse from(String buyerCode, long totalQuantity, long totalAmount, List<ItemInfo> items) {
        return new OrderValidateResponse(
                buyerCode,
                totalQuantity,
                BigDecimal.valueOf(totalAmount),
                items
        );
    }

    public record ItemInfo(
            String productCode,
            int requestedQuantity,
            int price
    ) {
        public static ItemInfo from(String productCode, int requestedQuantity, int price) {
            return new ItemInfo(productCode, requestedQuantity, price);
        }
    }
}

