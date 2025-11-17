package com.ll.payment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PaymentViewController {

    @Value("${payment.widgetClientKey}")
    private String widgetClientKey;

    @Value("${payment.successUrl}")
    private String successUrl;

    @Value("${payment.failUrl}")
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
        model.addAttribute("orderId", orderId);
        model.addAttribute("productName", orderName);
        model.addAttribute("amount", amount);
        model.addAttribute("customerName", customerName);
        model.addAttribute("clientKey", widgetClientKey);
        model.addAttribute("successUrl", successUrl);
        model.addAttribute("failUrl", failUrl);
        return "index";
    }

    /**
     * 결제 성공 페이지를 반환합니다.
     */
    @GetMapping("/success")
    public String successPage(
            @RequestParam(required = false) String paymentKey,
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String amount,
            Model model
    ) {
        model.addAttribute("paymentKey", paymentKey);
        model.addAttribute("orderId", orderId);
        model.addAttribute("amount", amount);
        return "success";
    }

    /**
     * 결제 실패 페이지를 반환합니다.
     */
    @GetMapping("/fail")
    public String failPage(
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String orderName,
            @RequestParam(required = false) String amount,
            Model model
    ) {
        model.addAttribute("orderId", orderId);
        model.addAttribute("productName", orderName);
        model.addAttribute("amount", amount);
        return "fail";
    }
}

