package com.ll.order.domain.controller;

import com.example.core.model.response.BaseResponse;
import com.ll.order.domain.model.vo.request.OrderCartItemRequest;
import com.ll.order.domain.model.vo.request.OrderDirectRequest;
import com.ll.order.domain.model.vo.request.OrderStatusUpdateRequest;
import com.ll.order.domain.model.vo.request.OrderValidateRequest;
import com.ll.order.domain.model.vo.response.*;
import com.ll.order.domain.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /*
    주문 생성 : 클라이언트 주문 생성 및 DB 저장
    → 결제 요청 : 주문 서비스에서 결제 서비스에 요청 및 응답 대기
    → 결제 처리 : 주문의 요청을 받아 처리. 외부 API 연동
    → 주문 상태 업데이트 : 결제 결과에 따라 주문 상태 변경 (created → paid / failed)
    * */
    @PostMapping("/cartItems")
    // TODO 토스 보완 결제 시 paymentKey 필수 여부와 검증 로직 추가 필요
    public ResponseEntity<BaseResponse<OrderCreateResponse>> createCartItemOrder(
            @Valid @RequestBody OrderCartItemRequest request
    ) {
        OrderCreateResponse response = orderService.createCartItemOrder(request);

        return BaseResponse.created(response);
    }

    @PostMapping("/direct")
    public ResponseEntity<BaseResponse<OrderCreateResponse>> createDirectOrder(
            @Valid @RequestBody OrderDirectRequest request
    ) {
        OrderCreateResponse response = orderService.createDirectOrder(request);

        return BaseResponse.created(response);
    }

    @GetMapping
    public ResponseEntity<BaseResponse<OrderPageResponse>> getOrderList(
            @RequestParam String userCode,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        OrderPageResponse orderPageResponse = orderService.findAllOrders(userCode, keyword, pageable);

        return BaseResponse.ok(orderPageResponse);
    }

    @GetMapping("/{orderCode}/details")
    // TODO 상품 상세 응답에 외부 상품 정보 포함하거나 불필요 호출 제거 검토
    public ResponseEntity<BaseResponse<OrderDetailResponse>> getOrderDetails(
            @PathVariable String orderCode
    ) {
        OrderDetailResponse response = orderService.findOrderDetails(orderCode);

        return BaseResponse.ok(response);
    }


    @PatchMapping("/{orderCode}/status")
    public ResponseEntity<BaseResponse<OrderStatusUpdateResponse>> updateOrderStatus(
            @PathVariable String orderCode,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        OrderStatusUpdateResponse response = orderService.updateOrderStatus(orderCode, request);

        return BaseResponse.ok(response);
    }

    // 주문 가능 여부 확인
    @PostMapping("/validate")
    public ResponseEntity<BaseResponse<OrderValidateResponse>> validateOrder(
            @RequestBody OrderValidateRequest request
    ) {
        OrderValidateResponse response = orderService.validateOrder(request);

        return BaseResponse.ok(response);
    }
}
