package com.ll.order.domain.controller;

import com.ll.order.domain.model.vo.request.OrderCartItemRequest;
import com.ll.order.domain.model.vo.request.OrderDirectRequest;
import com.ll.order.domain.model.vo.response.OrderCreateResponse;
import com.ll.order.domain.model.vo.response.OrderDetailResponse;
import com.ll.order.domain.model.vo.response.OrderPageResponse;
import com.ll.order.domain.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

//    @DeleteMapping("/{orderCode}")
//    public ResponseEntity deleteOrder(
//            @RequestParam String buyerCode,
//            @RequestParam String orderCode
//    ) {
//
//        return ResponseEntity.ok(null);
//    }
//

    //Pageable 객체로 controller 파라미터에서 바로 받을 수 있습니다!
    //@PageableDefault 어노테이션도 같이 참조해보시면 좋을 거 같아요
    @GetMapping
    public ResponseEntity<OrderPageResponse> getOrderList(
            @RequestParam String userCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        OrderPageResponse orderPageResponse = orderService.findAllOrders(userCode, keyword, page, size, sortBy, sortOrder);

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
//
//    @PostMapping
//    public ResponseEntity getPaymentRequest(
//            @RequestParam String buyerCode
//    ) {
//
//        return ResponseEntity.ok(null);
//    }
//

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
//            @RequestParam String buyerCode,
//            @RequestParam String orderCode
//    ) {
//
//        return ResponseEntity.ok(null);
//    }
//
//    @PostMapping("/{orderCode}/payment/validate")
//    public ResponseEntity validatePayment(
//            @RequestParam String buyerCode,
//            @RequestParam String orderCode
//    ) {
//
//        return ResponseEntity.ok(null);
//    }
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
