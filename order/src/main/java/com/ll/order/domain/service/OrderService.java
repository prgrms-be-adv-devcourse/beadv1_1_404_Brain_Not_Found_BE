package com.ll.order.domain.service;

import com.ll.order.domain.model.vo.request.OrderCartItemRequest;
import com.ll.order.domain.model.vo.request.OrderDirectRequest;
import com.ll.order.domain.model.vo.request.OrderStatusUpdateRequest;
import com.ll.order.domain.model.vo.request.OrderValidateRequest;
import com.ll.order.domain.model.vo.response.OrderCreateResponse;
import com.ll.order.domain.model.vo.response.OrderDetailResponse;
import com.ll.order.domain.model.vo.response.OrderPageResponse;
import com.ll.order.domain.model.vo.response.OrderStatusUpdateResponse;
import com.ll.order.domain.model.vo.response.OrderValidateResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderPageResponse findAllOrders(String userCode, String keyword, Pageable pageable);

    OrderDetailResponse findOrderDetails(String orderCode);

    OrderCreateResponse createCartItemOrder(OrderCartItemRequest request);

    OrderCreateResponse createDirectOrder(OrderDirectRequest request);

    OrderStatusUpdateResponse updateOrderStatus(String orderCode, @Valid OrderStatusUpdateRequest request);

    OrderValidateResponse validateOrder(OrderValidateRequest request);

    /**
     * paymentKey를 받아서 주문 결제를 완료 처리합니다.
     */
    void completePaymentWithKey(Long orderId, String paymentKey);
}
