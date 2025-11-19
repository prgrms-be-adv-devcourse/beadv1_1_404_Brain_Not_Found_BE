package com.ll.order.domain.controller;

import com.ll.order.domain.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class PaymentViewController {

    private final OrderService orderService;

    @Value("${payment.widgetClientKey:test_gck_docs_Ovk5rk1EwkEbP0W43n07xlzm}")
    private String widgetClientKey;

    @Value("${payment.successUrl:http://localhost:8082/api/orders/payment/success}")
    private String successUrl;

    @Value("${payment.failUrl:http://localhost:8082/api/orders/payment/fail}")
    private String failUrl;

    /**
     * 결제 페이지를 반환합니다.
     * 
     * @param orderId 주문 ID
     * @param orderName 주문명 (상품명)
     * @param amount 결제 금액
     * @param customerName 고객명
     * @param model 모델
     * @return 결제 페이지 템플릿
     */
    @GetMapping("/payment")
    public String paymentPage(
            @RequestParam Long orderId,
            @RequestParam String orderName,
            @RequestParam Integer amount,
            @RequestParam(required = false, defaultValue = "고객") String customerName,
            Model model
    ) {
        // orderId로 orderCode 조회
        String orderCode = orderService.getOrderCodeById(orderId);
        
        model.addAttribute("orderId", orderId);
        model.addAttribute("orderCode", orderCode);
        model.addAttribute("orderName", orderName);
        model.addAttribute("amount", amount);
        model.addAttribute("customerName", customerName);
        model.addAttribute("clientKey", widgetClientKey);
        model.addAttribute("successUrl", successUrl);
        model.addAttribute("failUrl", failUrl);
        return "payment";
    }

    /**
     * 결제 성공 페이지를 반환합니다.
     */
    @GetMapping("/payment/success-page")
    public String successPage(
            @RequestParam String orderId,
            @RequestParam String amount,
            Model model
    ) {
        model.addAttribute("orderId", orderId);
        model.addAttribute("amount", amount);
        return "payment-success";
    }

    /**
     * 결제 실패 페이지를 반환합니다.
     */
    @GetMapping("/payment/fail-page")
    public String failPage(
            @RequestParam(required = false) String errorCode,
            @RequestParam(required = false) String errorMessage,
            @RequestParam(required = false) String orderId,
            Model model
    ) {
        model.addAttribute("errorCode", errorCode);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("orderId", orderId);
        return "payment-fail";
    }
}

