package com.ll.order.domain.service;

import com.ll.order.domain.model.vo.request.OrderCartItemRequest;
import com.ll.order.domain.model.vo.request.OrderDirectRequest;
import com.ll.order.domain.model.vo.request.OrderStatusUpdateRequest;
import com.ll.order.domain.model.vo.request.OrderValidateRequest;
import com.ll.order.domain.model.vo.response.order.OrderCreateResponse;
import com.ll.order.domain.model.vo.response.order.OrderDetailResponse;
import com.ll.order.domain.model.vo.response.order.OrderPageResponse;
import com.ll.order.domain.model.vo.response.order.OrderStatusUpdateResponse;
import com.ll.order.domain.model.vo.response.order.OrderValidateResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderPageResponse findAllOrders(String userCode, String keyword, Pageable pageable);

    OrderDetailResponse findOrderDetails(String orderCode);

    OrderCreateResponse createCartItemOrder(OrderCartItemRequest request, String userCode);

    OrderCreateResponse createDirectOrder(OrderDirectRequest request, String userCode);

    OrderStatusUpdateResponse updateOrderStatus(String orderCode, @Valid OrderStatusUpdateRequest request, String userCode);

    OrderValidateResponse validateOrder(OrderValidateRequest request);

    /**
     * paymentKey를 받아서 주문 결제를 완료 처리합니다.
     */
    void completePaymentWithKey(String orderCode, String paymentKey);

    /**
     * orderId로 order code를 조회합니다.
     */
    String getOrderCodeById(Long orderId);
}
