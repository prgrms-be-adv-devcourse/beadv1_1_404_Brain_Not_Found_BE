package com.ll.order.domain.model.vo.request;

public record ProductRequest(
    String productCode,
    int quantity,
    String image
) {
}
