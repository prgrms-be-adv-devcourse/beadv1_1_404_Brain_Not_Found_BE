package com.ll.order.domain.model.vo.request;

import java.awt.*;

public record ProductRequest(
    String productCode,
    int quantity,
    Image image
) {
}
