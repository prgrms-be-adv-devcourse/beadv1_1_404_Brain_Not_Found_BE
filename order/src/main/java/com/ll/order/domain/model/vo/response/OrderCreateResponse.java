package com.ll.order.domain.model.vo.response;

import com.ll.order.domain.model.enums.OrderType;

public record OrderCreateResponse(
        String accountCode,
        ClientResponse client,
        ProductResponse products,
        int totalPrice,
        OrderType orderType
) {
}


