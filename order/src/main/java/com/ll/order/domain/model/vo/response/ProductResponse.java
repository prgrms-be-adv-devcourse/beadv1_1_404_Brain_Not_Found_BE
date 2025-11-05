package com.ll.order.domain.model.vo.response;

import java.awt.Image;

public record ProductResponse(
        Long productId,
        Integer quentity,
        Image image
) {
}


