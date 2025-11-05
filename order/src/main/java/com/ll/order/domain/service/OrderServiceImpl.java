package com.ll.order.domain.service;

import com.ll.order.domain.model.vo.request.OrderCreateRequest;
import com.ll.order.domain.model.vo.response.OrderDetailResponse;
import com.ll.order.domain.model.vo.response.OrderListApiResponse;
import com.ll.order.domain.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public List<OrderListApiResponse> findAllOrders(String userCode, int page, int size, String sort) {
        return List.of();
    }

    @Override
    public OrderDetailResponse findOrderDetails(String orderCode) {

        return null;
    }

    @Override
    public void createOrder(String userCode, OrderCreateRequest request) {
        // 기본 검증
        if (request == null) {
            throw new IllegalArgumentException("요청이 비어 있습니다.");
        }
        if (request.user() == null) {
            throw new IllegalArgumentException("사용자 정보가 필요합니다.");
        }
        if (request.totalPrice() <= 0) {
            throw new IllegalArgumentException("결제 금액이 0 이하입니다.");
        }
        if (request.orderType() == null) {
            throw new IllegalArgumentException("주문 유형이 필요합니다.");
        }

        // 매핑 준비
        String orderCode = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Long buyerId = parseLongSafely(request.user().userId(), "buyerId");
        String address = request.user().address();

        // 현 DTO에는 sellerId, cartId가 없어 저장 불가 → 임시로 검증 실패 처리
        // 실제로는 DTO 확장 또는 조회/연계로 값을 확보해야 함
        throwIfBlank(address, "address");

        // 필요한 필드가 모두 준비된 경우에만 저장
        // 아래는 예시로 sellerId/cartId를 외부에서 주입받는 구조가 갖춰진 후 활성화
        // Order order = Order.builder()
        //         .orderCode(orderCode)
        //         .buyerId(buyerId)
        //         .sellerId(sellerId)
        //         .cartId(cartId)
        //         .totalPrice(request.totalPrice())
        //         .orderType(request.orderType())
        //         .orderStatus(OrderStatus.CREATED)
        //         .address(address)
        //         .build();
        // orderJpaRepository.save(order);
    }

    private static Long parseLongSafely(String value, String fieldName) {
        try {
            return value == null ? null : Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + "가 숫자가 아닙니다.");
        }
    }

    private static void throwIfBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "가 비어 있습니다.");
        }
    }
}
