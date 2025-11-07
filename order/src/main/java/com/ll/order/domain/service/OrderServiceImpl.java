package com.ll.order.domain.service;

import com.ll.order.domain.client.CartServiceClient;
import com.ll.order.domain.client.ProductServiceClient;
import com.ll.order.domain.client.UserServiceClient;
import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.enums.OrderStatus;
import com.ll.order.domain.model.vo.request.OrderCartItemRequest;
import com.ll.order.domain.model.vo.request.OrderDirectRequest;
import com.ll.order.domain.model.vo.response.CartResponse;
import com.ll.order.domain.model.vo.response.ClientResponse;
import com.ll.order.domain.model.vo.response.OrderDetailResponse;
import com.ll.order.domain.model.vo.response.OrderListApiResponse;
import com.ll.order.domain.model.vo.response.ProductResponse;
import com.ll.order.domain.model.entity.OrderItem;
import com.ll.order.domain.repository.OrderItemJpaRepository;
import com.ll.order.domain.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderItemJpaRepository orderItemJpaRepository;
    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;
    private final CartServiceClient cartServiceClient;

    @Override
    public List<OrderListApiResponse> findAllOrders(String userCode, int page, int size, String sort) {
        return List.of();
    }

    @Override
    public OrderDetailResponse findOrderDetails(String orderCode) {
        Order order = orderJpaRepository.findByOrderCode(orderCode);
        if (order == null) {
            throw new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderCode);
        }

//        List<OrderItem> orderItems = orderItemJpaRepository.findByOrderId(order.getId());
//
//         for (OrderItem item : orderItems) {
//             ProductResponse product = productServiceClient.getProductById(item.getProductId());
//         }

        return new OrderDetailResponse(
                order.getId(),
                order.getOrderStatus(),
                order.getTotalPrice(),
                new OrderDetailResponse.UserInfo(
                        order.getBuyerId(),
                        order.getAddress()
                )
        );
    }

    @Override
    public void createCartItemOrder(OrderCartItemRequest request) {
        validateCartItemRequest(request);

        ClientResponse userInfo = userServiceClient.getUserByCode(request.userCode());
        if (userInfo == null) {
            throw new IllegalArgumentException("구매자를 찾을 수 없습니다: " + request.userCode());
        }

        CartResponse cartInfo = cartServiceClient.getCartByCode(request.cartCode());
        if (cartInfo == null) {
            throw new IllegalArgumentException("장바구니를 찾을 수 없습니다: " + request.cartCode());
        }

        if (cartInfo.items() == null || cartInfo.items().isEmpty()) {
            throw new IllegalArgumentException("장바구니가 비어있습니다: " + request.cartCode());
        }

        Map<String, ProductResponse> productMap = new HashMap<>();
        for (CartResponse.CartItemResponse item : cartInfo.items()) {
            ProductResponse productInfo = productServiceClient.getProductByCode(item.productCode());
            
            if (productInfo == null) {
                throw new IllegalArgumentException("상품을 찾을 수 없습니다: " + item.productCode());
            }
            
            productMap.put(item.productCode(), productInfo);
        }
        String orderCode = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        Order order = Order.builder()
                .orderCode(orderCode)
                .buyerId(userInfo.id())
                .totalPrice(request.totalPrice())
                .orderType(request.orderType())
                .orderStatus(OrderStatus.CREATED)
                .address(request.address())
                .build();

        Order savedOrder = orderJpaRepository.save(order);

        for (CartResponse.CartItemResponse cartItem : cartInfo.items()) {
            ProductResponse productInfo = productMap.get(cartItem.productCode());
            
            String orderItemCode = "ORD-ITEM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            OrderItem orderItem = OrderItem.builder()
                    .orderId(savedOrder.getId())
                    .productId(productInfo.productId())
                    .sellerId(productInfo.sellerId())
                    .orderItemCode(orderItemCode)
                    .quantity(cartItem.quantity())
                    .price(cartItem.price())
                    .build();
            
            orderItemJpaRepository.save(orderItem);
        }
    }

    @Override
    public Order createDirectOrder(OrderDirectRequest request) {
        validateDirectOrderRequest(request);

        ClientResponse userInfo = userServiceClient.getUserByCode(request.userCode());
        if (userInfo == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다: " + request.userCode());
        }

        ProductResponse productInfo = productServiceClient.getProductByCode(request.productCode());
        if (productInfo == null) {
            throw new IllegalArgumentException("상품을 찾을 수 없습니다: " + request.productCode());
        }

        String orderCode = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        Integer totalPrice = productInfo.totalPrice() * request.quantity();
        
        Order order = Order.builder()
                .orderCode(orderCode)
                .buyerId(userInfo.id())
                .totalPrice(totalPrice)
                .orderType(request.orderType())
                .orderStatus(OrderStatus.CREATED)
                .address(request.address())
                .build();

        Order savedOrder = orderJpaRepository.save(order);

        String orderItemCode = "ORD-ITEM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        OrderItem orderItem = OrderItem.builder()
                .orderId(savedOrder.getId())
                .productId(productInfo.productId())
                .sellerId(productInfo.sellerId())
                .orderItemCode(orderItemCode)
                .quantity(request.quantity())
                .price(productInfo.totalPrice())
                .build();
        
        orderItemJpaRepository.save(orderItem);

        return savedOrder;
    }

    private void validateCartItemRequest(OrderCartItemRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("요청이 비어 있습니다.");
        }
        if (request.userCode() == null || request.userCode().isBlank()) {
            throw new IllegalArgumentException("사용자 코드가 필요합니다.");
        }
        if (request.cartCode() == null || request.cartCode().isBlank()) {
            throw new IllegalArgumentException("카트 코드가 필요합니다.");
        }
        if (request.totalPrice() <= 0) {
            throw new IllegalArgumentException("결제 금액이 0 이하입니다.");
        }
        if (request.orderType() == null) {
            throw new IllegalArgumentException("주문 유형이 필요합니다.");
        }
        if (request.address() == null || request.address().isBlank()) {
            throw new IllegalArgumentException("배송지 주소가 필요합니다.");
        }
    }

    private void validateDirectOrderRequest(OrderDirectRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("요청이 비어 있습니다.");
        }
        if (request.userCode() == null || request.userCode().isBlank()) {
            throw new IllegalArgumentException("사용자 코드가 필요합니다.");
        }
        if (request.quantity() <= 0) {
            throw new IllegalArgumentException("주문 수량은 1 이상이어야 합니다.");
        }
        if (request.address() == null || request.address().isBlank()) {
            throw new IllegalArgumentException("배송지 주소가 필요합니다.");
        }
        if (request.productCode() == null || request.productCode().isBlank()) {
            throw new IllegalArgumentException("상품 코드가 필요합니다.");
        }
    }

}
