package com.ll.order.domain.model.vo.request;

public record UserRequest(
    String userId,
    String name,
    String address
) {
}
