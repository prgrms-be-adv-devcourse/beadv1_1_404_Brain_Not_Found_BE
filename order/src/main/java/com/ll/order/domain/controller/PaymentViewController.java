package com.ll.order.domain.controller;

import com.ll.order.domain.service.order.OrderService;
import jakarta.servlet.http.HttpSession;
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

    // 결재 페이지
    @GetMapping("/payment")
    public String paymentPage(
            @RequestParam Long orderId,
            @RequestParam String orderName,
            @RequestParam Integer amount, // 결재 금액
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

    @GetMapping("/payment/success-page")
    public String successPage(
            @RequestParam String orderId, // == orderCode
            @RequestParam String amount,
            Model model
    ) {
        model.addAttribute("orderId", orderId);
        model.addAttribute("amount", amount);
        return "payment-success";
    }

    @GetMapping("/payment/fail-page")
    public String failPage(
            @RequestParam(required = false) String errorCode,
            @RequestParam(required = false) String errorMessage,
            @RequestParam(required = false) String orderId, // == orderCode
            Model model
    ) {
        model.addAttribute("errorCode", errorCode);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("orderId", orderId);
        return "payment-fail";
    }

    // 주문 생성 폼 페이지
    @GetMapping("/create-form")
    public String orderFormPage() {
        return "order-form";
    }

    // 예치금 충전 폼 페이지
    @GetMapping("/deposit/charge-form")
    public String depositChargeFormPage() {
        return "deposit-charge-form";
    }

    // 예치금 충전 페이지 (토스 결제)
    @GetMapping("/deposit/charge")
    public String depositChargePage(
            @RequestParam Integer amount,
            @RequestParam String userCode,
            @RequestParam(required = false, defaultValue = "고객") String customerName,
            HttpSession session,
            Model model
    ) {
        // 세션에 userCode 저장 (결제 성공 시 사용)
        session.setAttribute("depositChargeUserCode", userCode);
        
        model.addAttribute("amount", amount);
        model.addAttribute("customerName", customerName);
        model.addAttribute("userCode", userCode);
        model.addAttribute("clientKey", widgetClientKey);
        model.addAttribute("successUrl", successUrl.replace("/payment/success", "/deposit/charge/success"));
        model.addAttribute("failUrl", failUrl.replace("/payment/fail", "/deposit/charge/fail"));
        return "deposit-charge";
    }

    @GetMapping("/deposit/charge/success-page")
    public String depositChargeSuccessPage(
            @RequestParam String amount,
            Model model
    ) {
        model.addAttribute("amount", amount);
        return "deposit-charge-success";
    }

    @GetMapping("/deposit/charge/fail-page")
    public String depositChargeFailPage(
            @RequestParam(required = false) String errorCode,
            @RequestParam(required = false) String errorMessage,
            Model model
    ) {
        model.addAttribute("errorCode", errorCode);
        model.addAttribute("errorMessage", errorMessage);
        return "deposit-charge-fail";
    }
}

