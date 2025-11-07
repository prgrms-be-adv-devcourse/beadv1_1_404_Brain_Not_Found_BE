package com.ll.order.domain.service;

import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.vo.request.OrderCartItemRequest;
import com.ll.order.domain.model.vo.request.OrderDirectRequest;
import com.ll.order.domain.model.vo.response.OrderDetailResponse;
import com.ll.order.domain.model.vo.response.OrderListApiResponse;

import java.util.List;

public interface OrderService {

    List<OrderListApiResponse> findAllOrders(String userCode, int page, int size, String sort);

    OrderDetailResponse findOrderDetails(String orderCode);

    Order createCartItemOrder(OrderCartItemRequest request);

    Order createDirectOrder(OrderDirectRequest request);
}
