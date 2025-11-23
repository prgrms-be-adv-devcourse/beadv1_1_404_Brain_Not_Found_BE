package com.ll.order.domain.model.vo.response.order;

import java.math.BigDecimal;
import java.util.List;

public record OrderValidateResponse(
        String buyerCode,
        long totalQuantity,
        BigDecimal totalAmount,
        List<ItemInfo> items
) {

    public record ItemInfo(
            String productCode,
            int requestedQuantity,
            int price
    ) {
    }
}

