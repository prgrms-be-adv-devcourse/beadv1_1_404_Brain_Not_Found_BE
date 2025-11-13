package com.ll.order.domain.service;

import com.ll.order.domain.client.CartServiceClient;
import com.ll.order.domain.client.PaymentServiceClient;
import com.ll.order.domain.client.ProductServiceClient;
import com.ll.order.domain.client.UserServiceClient;
import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.entity.OrderItem;
import com.ll.order.domain.model.enums.OrderStatus;
import com.ll.order.domain.model.vo.request.*;
import com.ll.order.domain.model.vo.response.*;
import com.ll.order.domain.repository.OrderItemJpaRepository;
import com.ll.order.domain.repository.OrderJpaRepository;
import com.ll.order.global.util.OrderCodeGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderItemJpaRepository orderItemJpaRepository;

    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;
    private final CartServiceClient cartServiceClient;
    private final PaymentServiceClient paymentApiClient;

    @Override
    @Transactional(readOnly = true)
    public OrderPageResponse findAllOrders(String userCode, String keyword, Pageable pageable) {
        ClientResponse userInfo = getUserInfo(userCode);

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
        Order order = Optional.ofNullable(orderJpaRepository.findByOrderCode(orderCode))
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderCode));

        List<OrderItem> orderItems = orderItemJpaRepository.findByOrderId(order.getId());

         for (OrderItem item : orderItems) {
             ProductResponse product = productServiceClient.getProductById(item.getProductId());
         }

        // 생성자 파라미터안에 생성자가 있는 구조는 가독성 측면에서 생각해봐야한다고 봅니다! 인스턴스화해서 변수로 활용하는 게 어떨까요?
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
    @Transactional
    public OrderCreateResponse createCartItemOrder(OrderCartItemRequest request) {
        ClientResponse userInfo = getUserInfo(request.buyerCode());

        CartResponse cartInfo = getCartInfo(request.cartCode());

        if (cartInfo.items() == null || cartInfo.items().isEmpty()) {
            throw new IllegalArgumentException("장바구니가 비어있습니다: " + request.cartCode());
        }

        //Map 보다 List<객체> 형식으로 사용하는 게 좀 더 확장성과 객체지향적이라고 생각합니다.
        Map<String, ProductResponse> productMap = new HashMap<>();
        for (CartResponse.CartItemResponse item : cartInfo.items()) {
            ProductResponse productInfo = getProductInfo(item.productCode());
            productMap.put(item.productCode(), productInfo);
        }

        Order order = Order.create(
                OrderCodeGenerator.newOrderCode(),
                userInfo.id(),
                request.orderType(),
                request.address()
        );
        Order savedOrder = orderJpaRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartResponse.CartItemResponse cartItem : cartInfo.items()) {
            ProductResponse productInfo = productMap.get(cartItem.productCode());

            OrderItem orderItem = savedOrder.createOrderItem(
                    productInfo.productId(),
                    productInfo.sellerId(),
                    OrderCodeGenerator.newOrderItemCode(),
                    productInfo.productName(),
                    cartItem.quantity(),
                    cartItem.price()
            );
            orderItems.add(orderItem);
        }

        orderItemJpaRepository.saveAll(orderItems);
        /*
        1) OrderCartItemRequest → 사용자 입력값 받기
        2) OrderServiceImpl에서 PaidType 유효성 검사
        3) OrderPaymentRequest에 PaidType 포함 → 결제 도메인 전달
        */
        OrderPaymentRequest orderPaymentRequest = new OrderPaymentRequest(
                savedOrder.getId(),
                savedOrder.getBuyerId(),
                request.buyerCode(),
                savedOrder.getTotalPrice(),
                request.paidType(),
                request.paymentKey()
        );

        // 결제 도메인 호출
        switch (request.paidType()) {
            case DEPOSIT -> paymentApiClient.requestDepositPayment(orderPaymentRequest);
            case TOSS_PAYMENT -> paymentApiClient.requestTossPayment(orderPaymentRequest);
            default -> throw new IllegalArgumentException("지원하지 않는 결제 수단입니다: " + request.paidType());
        }
        return convertToOrderCreateResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderCreateResponse createDirectOrder(OrderDirectRequest request) {
        ClientResponse userInfo = getUserInfo(request.userCode());

        ProductResponse productInfo = getProductInfo(request.productCode());

        Order order = Order.create(
                OrderCodeGenerator.newOrderCode(),
                userInfo.id(),
                request.orderType(),
                request.address()
        );
        Order savedOrder = orderJpaRepository.save(order);

        OrderItem orderItem = savedOrder.createOrderItem(
                productInfo.productId(),
                productInfo.sellerId(),
                OrderCodeGenerator.newOrderItemCode(),
                productInfo.productName(),
                request.quantity(),
                productInfo.totalPrice()
        );
        orderItemJpaRepository.save(orderItem);

        return convertToOrderCreateResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderStatusUpdateResponse updateOrderStatus(String orderCode, @Valid OrderStatusUpdateRequest request) {
        Order order = orderJpaRepository.findByOrderCode(orderCode);
        if (order == null) {
            throw new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderCode);
        }

        OrderStatus current = order.getOrderStatus();
        OrderStatus target = request.status();

        if (!current.canTransitionTo(target)) {
            throw new IllegalStateException("해당 상태로 전환할 수 없습니다: " + current + " -> " + target);
        }

        order.changeStatus(target);

        return new OrderStatusUpdateResponse(
                order.getOrderCode(),
                order.getOrderStatus(),
                order.getUpdatedAt()
        );
    }

    @Override
    public OrderValidateResponse validateOrder(OrderValidateRequest request) {
        ClientResponse userInfo = getUserInfo(request.buyerCode());

        if (userInfo.address() == null || userInfo.address().isBlank()) {
            throw new IllegalStateException("배송 가능한 주소가 없습니다: " + request.buyerCode());
        }

        Set<String> duplicatedCheck = new HashSet<>();
        int totalQuantity = 0;
        long totalAmount = 0L;
        List<OrderValidateResponse.ItemInfo> itemInfos = new ArrayList<>();

        for (ProductRequest productRequest : request.products()) {
            if (!duplicatedCheck.add(productRequest.productCode())) { // 이미 들어있는 코드면 false 반환
                throw new IllegalArgumentException("중복된 상품 코드가 포함되어 있습니다: " + productRequest.productCode());
            }

            ProductResponse productInfo = getProductInfo(productRequest.productCode());

            if (productInfo.quantity() < productRequest.quantity()) {
                throw new IllegalStateException("재고가 부족합니다. 상품 코드: " + productRequest.productCode());
            }

            if (productInfo.totalPrice() <= 0) {
                throw new IllegalStateException("유효하지 않은 상품 가격입니다. 상품 코드: " + productRequest.productCode());
            }

            if (productInfo.saleStatus() == null || !productInfo.saleStatus().isSaleable()) { // product enum이랑 일치해야 함.
                throw new IllegalStateException("판매 중이 아닌 상품입니다. 상품 코드: " + productRequest.productCode());
            }

            if (productRequest.price() != productInfo.totalPrice()) {
                throw new IllegalStateException("요청한 상품 가격이 실제 가격과 일치하지 않습니다. 상품 코드: " + productRequest.productCode());
            }

            totalQuantity += productRequest.quantity();
            totalAmount += (long) productInfo.totalPrice() * productRequest.quantity();

            itemInfos.add(new OrderValidateResponse.ItemInfo(
                    productRequest.productCode(),
                    productRequest.quantity(),
                    productInfo.totalPrice()
            ));
        }

        if (totalQuantity <= 0) {
            throw new IllegalArgumentException("주문 수량이 0 이하입니다.");
        }

        if (totalAmount <= 0) {
            throw new IllegalArgumentException("주문 금액이 0 이하입니다.");
        }

        return new OrderValidateResponse(
                request.buyerCode(),
                totalQuantity,
                BigDecimal.valueOf(totalAmount),
                itemInfos
        );
    }

    private ProductResponse getProductInfo(String productCode) {
        return Optional.ofNullable(productServiceClient.getProductByCode(productCode))
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productCode));
    }

    private OrderCreateResponse convertToOrderCreateResponse(Order order) {
        List<OrderCreateResponse.OrderItemInfo> orderItemInfoList = orderItemJpaRepository.findByOrderId(order.getId()).stream()
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

    private ClientResponse getUserInfo(String userCode) {
        return Optional.ofNullable(userServiceClient.getUserByCode(userCode))
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userCode));
    }

    private CartResponse getCartInfo(String cartCode) {
        return Optional.ofNullable(cartServiceClient.getCartByCode(cartCode))
                .orElseThrow(() -> new IllegalArgumentException("장바구니를 찾을 수 없습니다: " + cartCode));
    }

    // 도메인 별 규칙에 맞는 검증 로직이 필요하면 추가 작성할 것.
}
