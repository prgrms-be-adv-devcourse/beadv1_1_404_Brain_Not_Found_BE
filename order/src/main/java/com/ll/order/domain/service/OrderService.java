package com.ll.order.domain.service;

import com.ll.order.domain.model.vo.request.OrderCartItemRequest;
import com.ll.order.domain.model.vo.request.OrderDirectRequest;
import com.ll.order.domain.model.vo.response.OrderCreateResponse;
import com.ll.order.domain.model.vo.response.OrderDetailResponse;
import com.ll.order.domain.model.vo.response.OrderPageResponse;

public interface OrderService {

    OrderPageResponse findAllOrders(String userCode, String keyword, int page, int size, String sortBy, String sortOrder);

    OrderDetailResponse findOrderDetails(String orderCode);

    OrderCreateResponse createCartItemOrder(OrderCartItemRequest request);

    OrderCreateResponse createDirectOrder(OrderDirectRequest request);

}
