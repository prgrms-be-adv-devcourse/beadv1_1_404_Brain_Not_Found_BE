package com.ll.order.domain.controller;

import com.ll.core.model.response.BaseResponse;
import com.ll.order.domain.model.vo.request.OrderCartItemRequest;
import com.ll.order.domain.model.vo.request.OrderDirectRequest;
import com.ll.order.domain.model.vo.request.OrderStatusUpdateRequest;
import com.ll.order.domain.model.vo.response.order.*;
import com.ll.order.domain.service.order.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController implements OrderControllerSwagger {

    private final OrderService orderService;

    @Value("${current.domain}")
    private String currentDomain;

    @PostMapping("/cartItems")
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

    @GetMapping("/payment/success")
    public RedirectView paymentSuccess(
            @RequestParam String paymentKey,
            @RequestParam("orderId") String orderCode,
            @RequestParam String amount
    ) {
        try {
            // orderId 파라미터는 실제로 orderCode이므로 그대로 사용
            orderService.completePaymentWithKey(orderCode, paymentKey);
            return new RedirectView(currentDomain + "/orders/payment/success-page?orderId=" + orderCode + "&amount=" + amount);

        } catch (Exception e) {
            String encodedErrorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return new RedirectView(currentDomain + "/orders/payment/fail-page?error=" + encodedErrorMessage);
        }
    }

    @GetMapping("/payment/fail")
    public RedirectView paymentFail(
            @RequestParam(required = false) String errorCode,
            @RequestParam(required = false) String errorMessage,
            @RequestParam(required = false) String orderId // 토스 결제 위젯에서 전달되는 orderId는 실제로 orderCode입니다
    ) {
        return new RedirectView(currentDomain + "/orders/payment/fail-page?errorCode=" +
                (errorCode != null ? errorCode : "") +
                "&errorMessage=" + (errorMessage != null ? errorMessage : "") +
                "&orderId=" + (orderId != null ? orderId : ""));
    }

    @GetMapping("/deposit/charge/success")
    public RedirectView depositChargeSuccess(
            @RequestParam String paymentKey,
            @RequestParam("orderId") String orderId,
            @RequestParam String amount,
            HttpSession session
    ) {
        try {
            // 세션에서 userCode 가져오기
            String userCode = (String) session.getAttribute("depositChargeUserCode");
            if (userCode == null) {
                throw new IllegalArgumentException("사용자 코드를 찾을 수 없습니다. 세션이 만료되었을 수 있습니다.");
            }
            
            orderService.completeDepositChargeWithKey(userCode, paymentKey, Integer.parseInt(amount), orderId);
            
            // 세션에서 userCode 제거
            session.removeAttribute("depositChargeUserCode");
            
            return new RedirectView(currentDomain + "/orders/deposit/charge/success-page?amount=" + amount);
        } catch (Exception e) {
            String encodedErrorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return new RedirectView(currentDomain + "/orders/deposit/charge/fail-page?error=" + encodedErrorMessage);
        }
    }

    @GetMapping("/deposit/charge/fail")
    public RedirectView depositChargeFail(
            @RequestParam(required = false) String errorCode,
            @RequestParam(required = false) String errorMessage
    ) {
        return new RedirectView(currentDomain + "/orders/deposit/charge/fail-page?errorCode=" +
                (errorCode != null ? errorCode : "") +
                "&errorMessage=" + (errorMessage != null ? errorMessage : ""));
    }

    @PostMapping("/deposit/charge")
    public RedirectView initiateDepositCharge(
            @RequestBody Map<String, Object> request,
            @RequestHeader("X-User-Code") String userCode
    ) {
        Integer amount = (Integer) request.get("amount");
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }
        
        // 토스 결제 페이지로 리다이렉트
        String redirectUrl = String.format(currentDomain + "/orders/deposit/charge?amount=%d&userCode=%s",
                amount,
                URLEncoder.encode(userCode, StandardCharsets.UTF_8));
        return new RedirectView(redirectUrl);
    }

}
