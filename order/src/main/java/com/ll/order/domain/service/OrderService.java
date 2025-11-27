package com.ll.order.domain.service;

import com.ll.order.domain.model.enums.payment.PaidType;
import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.entity.OrderItem;
import com.ll.order.domain.model.vo.request.OrderCartItemRequest;
import com.ll.order.domain.model.vo.request.OrderDirectRequest;
import com.ll.order.domain.model.vo.request.OrderStatusUpdateRequest;
import com.ll.order.domain.model.vo.request.OrderValidateRequest;
import com.ll.order.domain.model.vo.response.order.OrderCreateResponse;
import com.ll.order.domain.model.vo.response.order.OrderCreationResult;
import com.ll.order.domain.model.vo.response.order.OrderDetailResponse;
import com.ll.order.domain.model.vo.response.order.OrderPageResponse;
import com.ll.order.domain.model.vo.response.order.OrderStatusUpdateResponse;
import com.ll.order.domain.model.vo.response.order.OrderValidateResponse;
import com.ll.order.domain.model.vo.response.user.UserResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    OrderPageResponse findAllOrders(String userCode, String keyword, Pageable pageable);

    OrderDetailResponse findOrderDetails(String orderCode);

    OrderCreateResponse createCartItemOrder(OrderCartItemRequest request, String userCode);

    OrderCreateResponse createDirectOrder(OrderDirectRequest request, String userCode);

    OrderStatusUpdateResponse updateOrderStatus(String orderCode, @Valid OrderStatusUpdateRequest request, String userCode);

    OrderValidateResponse validateOrder(OrderValidateRequest request);

//     paymentKey를 받아서 주문 결제를 완료 처리합니다.
    void completePaymentWithKey(String orderCode, String paymentKey);

    String getOrderCodeById(Long orderId);

    // 결제 타입에 따라 리다이렉트 URL을 생성합니다. 리다이렉트가 필요하지 않은 경우 Optional.empty()를 반환합니다.
    Optional<String> buildPaymentRedirectUrl(OrderCreateResponse response, PaidType paidType);

    /**
     * 주문 및 주문 상품 생성 (독립 트랜잭션)
     * 결제 실패와 무관하게 주문은 항상 저장됨
     */
    OrderCreationResult createOrderWithItems(OrderCartItemRequest request, UserResponse userInfo);

    /**
     * 예치금 결제 처리 (별도 트랜잭션)
     * 실패해도 주문에는 영향 없음 (주문은 이미 커밋됨)
     */
    void processDepositPayment(Order order, List<OrderItem> orderItems, OrderCartItemRequest request);

    /**
     * 직접 주문 및 주문 상품 생성 (독립 트랜잭션)
     * 결제 실패와 무관하게 주문은 항상 저장됨
     */
    OrderCreationResult createDirectOrderWithItem(OrderDirectRequest request, UserResponse userInfo);

    /**
     * 직접 주문 예치금 결제 처리 (별도 트랜잭션)
     * 실패해도 주문에는 영향 없음 (주문은 이미 커밋됨)
     */
    void processDirectDepositPayment(Order order, List<OrderItem> orderItems, OrderDirectRequest request);
}
