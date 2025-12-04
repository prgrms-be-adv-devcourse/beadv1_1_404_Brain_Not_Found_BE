package com.ll.order.domain.controller;

import com.ll.core.model.response.BaseResponse;
import com.ll.order.domain.model.vo.request.OrderCartItemRequest;
import com.ll.order.domain.model.vo.request.OrderDirectRequest;
import com.ll.order.domain.model.vo.request.OrderStatusUpdateRequest;
import com.ll.order.domain.model.vo.request.OrderValidateRequest;
import com.ll.order.domain.model.vo.response.order.*;
import com.ll.order.domain.service.order.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController implements OrderControllerSwagger {

    private final OrderService orderService;

    @PostMapping("/cartItems")
    // TODO 토스 보완 결제 시 paymentKey 필수 여부와 검증 로직 추가 필요
    public Object createCartItemOrder(
            @Valid @RequestBody OrderCartItemRequest request,
            @RequestHeader("X-User-Code") String userCode
    ) {
        OrderCreateResponse response = orderService.createCartItemOrder(request, userCode);

        return orderService.buildPaymentRedirectUrl(response, request.paidType())
                .map(RedirectView::new)
                .map(Object.class::cast)
                .orElse(BaseResponse.created(response));
    }

    @PostMapping("/direct")
    public Object createDirectOrder(
            @Valid @RequestBody OrderDirectRequest request,
                @RequestHeader("X-User-Code") String userCode
    ) {
        OrderCreateResponse response = orderService.createDirectOrder(request, userCode);

        return orderService.buildPaymentRedirectUrl(response, request.paidType())
                .map(RedirectView::new)
                .map(Object.class::cast)
                .orElse(BaseResponse.created(response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<OrderPageResponse>> getOrderList(
            @RequestHeader("X-User-Code") String userCode,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        OrderPageResponse orderPageResponse = orderService.findAllOrders(userCode, keyword, pageable);

        return BaseResponse.ok(orderPageResponse);
    }

    @GetMapping("/{orderCode}/details")
    // TODO 상품 상세 응답에 외부 상품 정보 포함하거나 불필요 호출 제거 검토
    public ResponseEntity<BaseResponse<OrderDetailResponse>> getOrderDetails(
            @PathVariable String orderCode,
                @RequestHeader("X-User-Code") String userCode
    ) {
        OrderDetailResponse response = orderService.findOrderDetails(orderCode);

        return BaseResponse.ok(response);
    }

    @PatchMapping("/{orderCode}/status")
    public ResponseEntity<BaseResponse<OrderStatusUpdateResponse>> updateOrderStatus(
            @PathVariable String orderCode,
            @Valid @RequestBody OrderStatusUpdateRequest request,
                @RequestHeader("X-User-Code") String userCode
    ) {
        OrderStatusUpdateResponse response = orderService.updateOrderStatus(orderCode, request, userCode);

        return BaseResponse.ok(response);
    }

    @GetMapping("/{orderId}/code")
    public ResponseEntity<BaseResponse<Map<String, String>>> getOrderCodeById(
            @PathVariable Long orderId
    ) {
        String orderCode = orderService.getOrderCodeById(orderId);
        return BaseResponse.ok(Map.of("orderCode", orderCode));
    }

    // 주문 가능 여부 확인
    @PostMapping("/validate")
    public ResponseEntity<BaseResponse<OrderValidateResponse>> validateOrder(
            @Valid @RequestBody OrderValidateRequest request
    ) {
        OrderValidateResponse response = orderService.validateOrder(request);
        return BaseResponse.ok(response);
    }

    @GetMapping("/payment/success")
    public RedirectView paymentSuccess(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam String amount
    ) {
        try {
            orderService.completePaymentWithKey(orderId, paymentKey);
            return new RedirectView("/orders/payment/success-page?orderId=" + orderId + "&amount=" + amount);
        } catch (Exception e) {
            return new RedirectView("/orders/payment/fail-page?error=" + e.getMessage());
        }
    }

    @GetMapping("/payment/fail")
    public RedirectView paymentFail(
            @RequestParam(required = false) String errorCode,
            @RequestParam(required = false) String errorMessage,
            @RequestParam(required = false) String orderId
    ) {
        return new RedirectView("/orders/payment/fail-page?errorCode=" + 
               (errorCode != null ? errorCode : "") + 
               "&errorMessage=" + (errorMessage != null ? errorMessage : "") +
               "&orderId=" + (orderId != null ? orderId : ""));
    }

}
