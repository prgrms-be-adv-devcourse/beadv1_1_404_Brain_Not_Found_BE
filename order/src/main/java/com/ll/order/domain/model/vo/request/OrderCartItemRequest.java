package com.ll.order.domain.model.vo.request;

import com.ll.order.domain.model.enums.OrderType;

import java.util.List;

public record OrderCartItemRequest(
    String userCode,
    String cartCode,
    String name,
    String address,
    List<ProductRequest> products,
    int totalPrice,
    OrderType orderType
) {
}
