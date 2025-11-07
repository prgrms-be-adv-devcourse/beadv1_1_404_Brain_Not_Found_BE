package com.ll.order.domain.controller;

import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.vo.request.OrderCartItemRequest;
import com.ll.order.domain.model.vo.request.OrderDirectRequest;
import com.ll.order.domain.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    /*
    주문 생성 : 클라이언트 주문 생성 및 DB 저장
    → 결제 요청 : 주문 서비스에서 결제 서비스에 요청 및 응답 대기
    → 결제 처리 : 주문의 요청을 받아 처리. 외부 API 연동
    → 주문 상태 업데이트 : 결제 결과에 따라 주문 상태 변경 (created → paid / failed)
    * */

    private final OrderService orderService;

    @PostMapping("/cartItems")
    public ResponseEntity<Order> createCartItemOrder(
            @RequestBody OrderCartItemRequest request
    ) {
        Order cartItemOrder = orderService.createCartItemOrder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cartItemOrder);
    }

    @PostMapping("/direct")
    public ResponseEntity<Order> createDirectOrder(
            @RequestBody OrderDirectRequest request
    ) {
        Order directOrder = orderService.createDirectOrder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(directOrder);
    }

//    @DeleteMapping("/{orderCode}")
//    public ResponseEntity deleteOrder(
//            @RequestParam String userCode,
//            @RequestParam String orderCode
//    ) {
//
//        return ResponseEntity.ok(null);
//    }
//
//    @GetMapping
//    public ResponseEntity<List<OrderListApiResponse>> getOrderList(
//            @RequestParam String userCode,
//            @RequestParam int page, @RequestParam int size, @RequestParam String sort
//    ) {
//        List<OrderListApiResponse> allOrders = orderService.findAllOrders(userCode, page, size, sort);
//
//        return ResponseEntity
//                .status(HttpStatus.OK)
//                .body(allOrders);
//    }
//
//    @PostMapping
//    public ResponseEntity getPaymentRequest(
//            @RequestParam String userCode
//    ) {
//
//        return ResponseEntity.ok(null);
//    }
//
//    @GetMapping("/{orderCode}/details")
//    public ResponseEntity<OrderDetailResponse> getOrderDetails(
//            @RequestParam String orderCode
//    ) {
//        OrderDetailResponse orderDetails = orderService.findOrderDetails(orderCode);
//
//        return ResponseEntity
//                .status(HttpStatus.OK)
//                .body(orderDetails);
//    }
//
//    @PatchMapping("/{orderCode}/status")
//    public ResponseEntity updateOrderStatus(
//            @RequestParam String orderCode
//    ) {
//
//        return ResponseEntity.ok(null);
//    }
//
//    @PostMapping("/{orderCode}/payment/complete")
//    public ResponseEntity completePayment(
//            @RequestParam String userCode,
//            @RequestParam String orderCode
//    ) {
//
//        return ResponseEntity.ok(null);
//    }
//
//    @PostMapping("/{orderCode}/payment/validate")
//    public ResponseEntity validatePayment(
//            @RequestParam String userCode,
//            @RequestParam String orderCode
//    ) {
//
//        return ResponseEntity.ok(null);
//    }
//
//    @GetMapping("/search")
//    public ResponseEntity searchOrders(
//            @RequestParam String userCode,
//            @RequestParam String searchType, @RequestParam String searchValue,
//            @RequestParam int page, @RequestParam int size, @RequestParam String sort
//    ) {
//
//        return ResponseEntity.ok(null);
//    }
//
//    @PostMapping("/{orderCode}/complete")
//    public ResponseEntity completeOrder(
//            @RequestParam String userCode,
//            @RequestParam String orderCode
//    ) {
//
//
//        return ResponseEntity.ok(null);
//    }
}
