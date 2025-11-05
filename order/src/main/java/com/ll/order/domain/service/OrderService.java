package com.ll.order.domain.service;

import com.ll.order.domain.model.vo.request.OrderCreateRequest;
import com.ll.order.domain.model.vo.response.OrderDetailResponse;
import com.ll.order.domain.model.vo.response.OrderListApiResponse;

import java.util.List;

public interface OrderService {

    List<OrderListApiResponse> findAllOrders(String userCode, int page, int size, String sort);

    OrderDetailResponse findOrderDetails(String orderCode);

    void createOrder(String userCode, OrderCreateRequest request);
}
