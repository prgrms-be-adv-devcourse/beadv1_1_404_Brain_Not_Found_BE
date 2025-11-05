package com.ll.order.domain.model.vo.request;

import com.ll.order.domain.model.enums.OrderType;

import java.util.List;

public record OrderCreateRequest(
    String userCode,
    UserRequest user,
    List<ProductRequest> products,
    int totalPrice,
    OrderType orderType
) {
}
