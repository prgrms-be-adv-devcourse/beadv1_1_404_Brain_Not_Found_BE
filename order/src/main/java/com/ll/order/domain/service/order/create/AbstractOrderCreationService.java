package com.ll.order.domain.service.order.create;

import com.ll.core.model.exception.BaseException;
import com.ll.order.domain.client.CartServiceClient;
import com.ll.order.domain.client.PaymentServiceClient;
import com.ll.order.domain.client.ProductServiceClient;
import com.ll.order.domain.client.UserServiceClient;
import com.ll.order.domain.exception.OrderErrorCode;
import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.entity.OrderItem;
import com.ll.order.domain.model.enums.payment.PaidType;
import com.ll.order.domain.model.vo.InventoryDeduction;
import com.ll.order.domain.model.vo.response.cart.CartItemsResponse;
import com.ll.order.domain.model.vo.response.order.OrderCreateResponse;
import com.ll.order.domain.model.vo.response.order.OrderCreationResult;
import com.ll.order.domain.model.vo.response.product.ProductResponse;
import com.ll.order.domain.model.vo.response.user.UserResponse;
import com.ll.order.domain.repository.OrderHistoryJpaRepository;
import com.ll.order.domain.repository.OrderItemJpaRepository;
import com.ll.order.domain.repository.OrderJpaRepository;
import com.ll.order.domain.service.event.OrderEventService;
import com.ll.order.domain.service.inventory.OrderInventoryService;
import com.ll.order.domain.service.order.OrderValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractOrderCreationService {

    // 공통 의존성 (protected로 선언하여 하위 클래스에서 접근 가능)
    protected final OrderJpaRepository orderJpaRepository;
    protected final OrderItemJpaRepository orderItemJpaRepository;
    protected final OrderHistoryJpaRepository orderHistoryJpaRepository;

    protected final UserServiceClient userServiceClient;
    protected final ProductServiceClient productServiceClient;
    protected final CartServiceClient cartServiceClient;
    protected final PaymentServiceClient paymentApiClient;

    protected final OrderValidator orderValidator;
    
    protected final OrderEventService orderEventService;
    protected final OrderInventoryService orderInventoryService;

    public final OrderCreateResponse createOrder(Object request, String userCode) {
        UserResponse userInfo = getUserInfo(userCode);

        // 1. 주문 생성 전 재고 가용성 체크 (읽기만, 락 없음)
        validateInventory(request);

        // 2. 주문 및 주문 상품 데이터 생성
        OrderCreationResult creationResult = createOrderWithItems(request, userInfo);
        Order savedOrder = creationResult.order();
        List<OrderItem> orderItems = creationResult.orderItems();

        // 3. 재고 차감 (주문 생성 후, 결제 전) <- 락 적용
        updateProductInventory(savedOrder, orderItems);

        // 4. 결제 처리 (별도 트랜잭션)
        PaidType paidType = extractPaidType(request);
        switch (paidType) {
            case DEPOSIT:
                processDepositPayment(savedOrder, orderItems, request);
                break;
            case TOSS_PAYMENT:
                // 토스 결제: 주문만 생성하고 결제는 UI에서 처리
                // 주문 상태는 CREATED로 유지 (결제 완료 후 completePaymentWithKey에서 COMPLETED로 변경)
                log.debug("토스 결제 주문 생성 완료 - orderId: {}, orderCode: {}, 상태: CREATED (결제 대기)",
                        savedOrder.getId(), savedOrder.getCode());
                break;
            default:
                log.warn("지원하지 않는 결제 수단입니다. paidType: {}", paidType);
                throw new BaseException(OrderErrorCode.UNSUPPORTED_PAYMENT_TYPE);
        }

        return convertToOrderCreateResponse(savedOrder);
    }

    // ========== 추상 메서드들 (하위 클래스에서 구현해야 함) ==========

    protected abstract void validateInventory(Object request);

    protected abstract OrderCreationResult createOrderWithItems(Object request, UserResponse userInfo);

    protected abstract void processDepositPayment(Order order, List<OrderItem> orderItems, Object request);

    protected abstract PaidType extractPaidType(Object request);

    protected abstract String extractPaymentKey(Object request);

    // ========== 공통 메서드들 (하위 클래스에서 사용 가능) ==========
    protected UserResponse getUserInfo(String userCode) {
        return Optional.ofNullable(userServiceClient.getUserByCode(userCode))
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없습니다. userCode: {}", userCode);
                    return new BaseException(OrderErrorCode.USER_NOT_FOUND);
                });
    }

    protected ProductResponse getProductInfo(String productCode) {
        return Optional.ofNullable(productServiceClient.getProductByCode(productCode))
                .orElseThrow(() -> {
                    log.warn("상품을 찾을 수 없습니다. productCode: {}", productCode);
                    return new BaseException(OrderErrorCode.PRODUCT_NOT_FOUND);
                });
    }

    protected CartItemsResponse getCartInfo(String cartCode) {
        CartItemsResponse cartInfo = Optional.ofNullable(cartServiceClient.getCartByCode(cartCode))
                .orElseThrow(() -> {
                    log.warn("장바구니를 찾을 수 없습니다. cartCode: {}", cartCode);
                    return new BaseException(OrderErrorCode.CART_NOT_FOUND);
                });

        if (cartInfo.isEmpty()) {
            log.warn("장바구니가 비어있습니다. cartCode: {}", cartCode);
            throw new BaseException(OrderErrorCode.CART_EMPTY);
        }

        return cartInfo;
    }

    protected void updateProductInventory(Order order, List<OrderItem> orderItems) {
        List<String> failedProducts = new ArrayList<>();
        List<InventoryDeduction> successfulDeductions = new ArrayList<>();

        for (OrderItem orderItem : orderItems) {
            // 재고 감소 (동기 API 호출) <- 비관적 락 적용 시점
            try {
                productServiceClient.decreaseInventory(orderItem.getProductCode(), orderItem.getQuantity());
                log.debug("재고 차감 완료 - productCode: {}, quantity: {}",
                        orderItem.getProductCode(), orderItem.getQuantity());
                // 성공한 재고 차감 정보 저장 (롤백용)
                successfulDeductions.add(new InventoryDeduction(
                        orderItem.getProductCode(),
                        orderItem.getQuantity()
                ));
            } catch (Exception e) {
                log.error("재고 차감 실패 - productCode: {}, quantity: {}, error: {}",
                        orderItem.getProductCode(), orderItem.getQuantity(), e.getMessage(), e);
                failedProducts.add(orderItem.getProductCode());
            }
        }

        // 재고 차감 실패 시 성공한 재고 차감 롤백
        if (!failedProducts.isEmpty()) {
            log.error("재고 차감 실패 - orderCode: {}, failedProducts: {}", order.getCode(), failedProducts);

            // 성공한 재고 차감 롤백
            if (!successfulDeductions.isEmpty()) {
                orderInventoryService.rollbackInventory(successfulDeductions, order.getCode());
            }

            throw new BaseException(OrderErrorCode.INVENTORY_DEDUCTION_FAILED);
        }
    }

    protected OrderCreateResponse convertToOrderCreateResponse(Order order) {
        List<OrderItem> orderItems = orderItemJpaRepository.findByOrderId(order.getId());
        return OrderCreateResponse.from(order, orderItems);
    }

}






