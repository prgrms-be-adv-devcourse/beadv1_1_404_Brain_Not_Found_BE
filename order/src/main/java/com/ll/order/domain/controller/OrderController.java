package com.ll.order.domain.controller;

import com.ll.order.domain.model.vo.request.OrderCartItemRequest;
import com.ll.order.domain.model.vo.request.OrderDirectRequest;
import com.ll.order.domain.model.vo.request.OrderStatusUpdateRequest;
import com.ll.order.domain.model.vo.request.OrderValidateRequest;
import com.ll.order.domain.model.vo.response.OrderCreateResponse;
import com.ll.order.domain.model.vo.response.OrderDetailResponse;
import com.ll.order.domain.model.vo.response.OrderPageResponse;
import com.ll.order.domain.model.vo.response.OrderStatusUpdateResponse;
import com.ll.order.domain.model.vo.response.OrderValidateResponse;
import com.ll.order.domain.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<OrderCreateResponse> createCartItemOrder(
            @Valid @RequestBody OrderCartItemRequest request
    ) {
        OrderCreateResponse response = orderService.createCartItemOrder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/direct")
    public ResponseEntity<OrderCreateResponse> createDirectOrder(
            @Valid @RequestBody OrderDirectRequest request
    ) {
        OrderCreateResponse response = orderService.createDirectOrder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<OrderPageResponse> getOrderList(
            @RequestParam String userCode,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        OrderPageResponse orderPageResponse = orderService.findAllOrders(userCode, keyword, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(orderPageResponse);
    }

    @GetMapping("/{orderCode}/details")
    public ResponseEntity<OrderDetailResponse> getOrderDetails(
            @PathVariable String orderCode
    ) {
        OrderDetailResponse response = orderService.findOrderDetails(orderCode);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }


    @PatchMapping("/{orderCode}/status")
    public ResponseEntity<OrderStatusUpdateResponse> updateOrderStatus(
            @PathVariable String orderCode,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        OrderStatusUpdateResponse response = orderService.updateOrderStatus(orderCode, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    // 주문 가능 여부 확인
    @PostMapping("/validate")
    public ResponseEntity<OrderValidateResponse> validateOrder(
            @RequestBody OrderValidateRequest request
    ) {
        OrderValidateResponse response = orderService.validateOrder(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
//
//
//    @PostMapping("/{orderCode}/complete")
//    public ResponseEntity completeOrder(
//            @RequestParam String buyerCode,
//            @RequestParam String orderCode
//    ) {
//
//
//        return ResponseEntity.ok(null);
//    }
}
