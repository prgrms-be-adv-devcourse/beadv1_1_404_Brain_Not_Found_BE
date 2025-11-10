package com.ll.order.domain.service;

import com.github.f4b6a3.uuid.UuidCreator;
import com.ll.order.domain.client.CartServiceClient;
import com.ll.order.domain.client.ProductServiceClient;
import com.ll.order.domain.client.UserServiceClient;
import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.entity.OrderItem;
import com.ll.order.domain.model.enums.OrderStatus;
import com.ll.order.domain.model.vo.request.OrderCartItemRequest;
import com.ll.order.domain.model.vo.request.OrderDirectRequest;
import com.ll.order.domain.model.vo.response.*;
import com.ll.order.domain.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderJpaRepository orderJpaRepository;
    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;
    private final CartServiceClient cartServiceClient;

    @Override
    public OrderPageResponse findAllOrders(String userCode, String keyword, int page, int size, String sortBy, String sortOrder) {
        ClientResponse userInfo = getUserInfo(userCode);

        Sort.Direction direction = sortOrder.equalsIgnoreCase("asc") 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        // keyword가 있으면 상품명으로 검색, 없으면 전체 조회
        Page<Order> orderPage;
        if (keyword != null && !keyword.isBlank()) {
            orderPage = orderJpaRepository.findByBuyerIdAndProductNameContaining(
                    userInfo.id(), keyword, pageable);
        } else {
            orderPage = orderJpaRepository.findByBuyerId(userInfo.id(), pageable);
        }

        // Order 엔티티를 OrderInfo DTO로 변환
        List<OrderPageResponse.OrderInfo> orderInfoList = orderPage.getContent().stream()
                .map(order -> new OrderPageResponse.OrderInfo(
                        order.getId(),
                        order.getOrderCode(),
                        order.getOrderStatus(),
                        order.getTotalPrice(),
                        new OrderPageResponse.UserInfo(
                                order.getBuyerId(),
                                order.getAddress()
                        )
                ))
                .collect(Collectors.toList());

        OrderPageResponse.PageInfo pageInfo = new OrderPageResponse.PageInfo(
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.hasNext(),
                orderPage.hasPrevious()
        );

        return new OrderPageResponse(orderInfoList, pageInfo);
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
    public OrderCreateResponse createCartItemOrder(OrderCartItemRequest request) {
        validateCartItemRequest(request);

        ClientResponse userInfo = getUserInfo(request.userCode());

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
        String orderCode = "ORD-" + UuidCreator.getTimeOrderedEpoch().toString().substring(0, 8).toUpperCase();
        
        Order order = Order.builder()
                .orderCode(orderCode)
                .buyerId(userInfo.id())
                .totalPrice(request.totalPrice())
                .orderType(request.orderType())
                .orderStatus(OrderStatus.CREATED)
                .address(request.address())
                .build();

        for (CartResponse.CartItemResponse cartItem : cartInfo.items()) {
            ProductResponse productInfo = productMap.get(cartItem.productCode());
            
            String orderItemCode = "ORD-ITEM-" + UuidCreator.getTimeOrderedEpoch().toString().substring(0, 8).toUpperCase();
            
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productId(productInfo.productId())
                    .sellerId(productInfo.sellerId())
                    .orderItemCode(orderItemCode)
                    .productName(productInfo.productName())
                    .quantity(cartItem.quantity())
                    .price(cartItem.price())
                    .build();
            
            order.addOrderItem(orderItem);
        }
        
        Order savedOrder = orderJpaRepository.save(order);
        return convertToOrderCreateResponse(savedOrder);
    }

    @Override
    public OrderCreateResponse createDirectOrder(OrderDirectRequest request) {
        validateDirectOrderRequest(request);

        ClientResponse userInfo = getUserInfo(request.userCode());

        ProductResponse productInfo = productServiceClient.getProductByCode(request.productCode());
        if (productInfo == null) {
            throw new IllegalArgumentException("상품을 찾을 수 없습니다: " + request.productCode());
        }

        String orderCode = "ORD-" + UuidCreator.getTimeOrderedEpoch().toString().substring(0, 8).toUpperCase();
        
        Integer totalPrice = productInfo.totalPrice() * request.quantity();
        
        Order order = Order.builder()
                .orderCode(orderCode)
                .buyerId(userInfo.id())
                .totalPrice(totalPrice)
                .orderType(request.orderType())
                .orderStatus(OrderStatus.CREATED)
                .address(request.address())
                .build();

        String orderItemCode = "ORD-ITEM-" + UuidCreator.getTimeOrderedEpoch().toString().substring(0, 8).toUpperCase();
        
        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .productId(productInfo.productId())
                .sellerId(productInfo.sellerId())
                .orderItemCode(orderItemCode)
                .productName(productInfo.productName())
                .quantity(request.quantity())
                .price(productInfo.totalPrice())
                .build();
        
        order.addOrderItem(orderItem);
        
        Order savedOrder = orderJpaRepository.save(order);

        return convertToOrderCreateResponse(savedOrder);
    }

    private OrderCreateResponse convertToOrderCreateResponse(Order order) {
        List<OrderCreateResponse.OrderItemInfo> orderItemInfoList = order.getOrderItems().stream()
                .map(item -> new OrderCreateResponse.OrderItemInfo(
                        item.getId(),
                        item.getOrderItemCode(),
                        item.getProductId(),
                        item.getSellerId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());

        return new OrderCreateResponse(
                order.getId(),
                order.getOrderCode(),
                order.getOrderStatus(),
                order.getTotalPrice(),
                order.getOrderType(),
                order.getAddress(),
                order.getBuyerId(),
                orderItemInfoList
        );
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

    private ClientResponse getUserInfo(String userCode) {
        ClientResponse userInfo = userServiceClient.getUserByCode(userCode);
        if (userInfo == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userCode);
        }
        return userInfo;
    }

}
