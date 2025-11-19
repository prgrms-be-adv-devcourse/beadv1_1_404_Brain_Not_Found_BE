package com.ll.order.domain.controller;

import com.ll.core.model.response.BaseResponse;
import com.ll.order.domain.model.vo.request.OrderCartItemRequest;
import com.ll.order.domain.model.vo.request.OrderDirectRequest;
import com.ll.order.domain.model.vo.request.OrderStatusUpdateRequest;
import com.ll.order.domain.model.vo.request.OrderValidateRequest;
import com.ll.order.domain.model.vo.response.*;
import com.ll.order.domain.service.OrderService;
import com.ll.payment.model.enums.PaidType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/ping")
    public ResponseEntity<BaseResponse<String>> pong() {
        System.out.println("OrderController.pong");
        return BaseResponse.ok("Ok");
    }

    /*
    주문 생성 : 클라이언트 주문 생성 및 DB 저장
    → 결제 요청 : 주문 서비스에서 결제 서비스에 요청 및 응답 대기
    → 결제 처리 : 주문의 요청을 받아 처리. 외부 API 연동
    → 주문 상태 업데이트 : 결제 결과에 따라 주문 상태 변경 (created → paid / failed)
    * */
    
    /*
    ====================================================================================
    [토스 결제를 사용한 주문 생성 및 결제 프로세스]
    ====================================================================================

    [전체 흐름]
    1. 주문 생성 (POST)
       - 토스 결제: 서버에서 자동으로 결제 페이지로 리다이렉트
       - 예치금 결제: JSON 응답 반환 (결제 완료)
    2. 결제 페이지에서 토스 결제 위젯 사용 (토스 결제인 경우)
    3. 토스 결제 완료 후 콜백 처리
    
    [사용 방법]
    ┌─────────────────────────────────────────────────────────────────────────────┐
    │ 1단계: 주문 생성 (POST 요청)                                                │
    └─────────────────────────────────────────────────────────────────────────────┘
    
    POST /api/orders/direct
    Content-Type: application/json
    
    {
      "userCode": "USER-001",
      "productCode": "PROD-001",
      "quantity": 2,
      "address": "서울시 강남구",
      "orderType": "ONLINE",
      "paidType": "TOSS_PAYMENT",  // 토스 결제인 경우
      "paymentKey": null           // 주문 생성 시에는 null
    }
    
    [응답 예시]
    {
      "status": 201,
      "message": "created",
      "data": {
        "id": 1,// ← 이 값을 사용하여 결제 페이지로 이동
        "orderCode": "ORDER-xxx",
        "orderStatus": "CREATED",
        "totalPrice": 20000,
        "orderType": "ONLINE",
        "address": "서울시 강남구",
        "buyerId": 1,
        "orderItems": [...]
      }
    }
    
    ┌─────────────────────────────────────────────────────────────────────────────┐
    │ 2단계: 결제 페이지로 이동 (토스 결제인 경우 자동 리다이렉트)                │
    └─────────────────────────────────────────────────────────────────────────────┘
    
    [토스 결제인 경우]
    - 주문 생성 API 호출 시 서버에서 자동으로 결제 페이지로 리다이렉트됩니다.
    - 프론트엔드에서 별도 처리 불필요 (브라우저가 자동으로 리다이렉트)
    
    [예치금 결제인 경우]
    - 주문 생성과 동시에 결제가 완료되므로 JSON 응답만 반환됩니다.
    - 프론트엔드에서 응답을 받아 완료 처리하면 됩니다.
    
    [직접 URL 접근 (테스트/디버깅용)]
    주문 ID를 알고 있는 경우 브라우저 주소창에 직접 입력하거나 링크로 접근할 수 있습니다.
    
    GET /orders/payment?orderId=1&orderName=상품명&amount=10000&customerName=홍길동
    
    ※ 주의: orderId, orderName, amount는 주문 생성 API 응답에서 받은 실제 값을 사용해야 합니다.
    
    ┌─────────────────────────────────────────────────────────────────────────────┐
    │ 3단계: 결제 페이지에서 토스 결제                                            │
    └─────────────────────────────────────────────────────────────────────────────┘
    
    - 결제 페이지(/orders/payment)에서 토스 결제 위젯이 자동으로 로드됩니다.
    - 사용자가 결제 수단을 선택하고 결제를 진행합니다.
    - 토스 서버에서 결제 처리가 완료되면 자동으로 콜백 URL로 리다이렉트됩니다.
    
    ┌─────────────────────────────────────────────────────────────────────────────┐
    │ 4단계: 결제 완료 콜백 처리 (토스 서버에서 자동 호출)                        │
    └─────────────────────────────────────────────────────────────────────────────┘
    
    토스 결제가 완료되면 토스 서버가 자동으로 아래 URL로 리다이렉트합니다.
    (프론트엔드나 사용자가 직접 호출하지 않습니다)
    
    [결제 성공 시]
    토스 서버가 자동으로 호출:
    GET /api/orders/payment/success?paymentKey=xxx&orderId=ORDER-1&amount=10000
    
    [결제 실패 시]
    토스 서버가 자동으로 호출:
    GET /api/orders/payment/fail?errorCode=xxx&errorMessage=xxx&orderId=ORDER-1
    
    ※ 이 URL은 payment.html에서 토스 위젯 초기화 시 successUrl, failUrl로 설정됩니다.
    ※ application.yml의 payment.successUrl, payment.failUrl 설정값이 사용됩니다.
    
    [주의사항]
    - 주문 생성 시 paidType이 TOSS_PAYMENT인 경우, paymentKey는 null로 전달해야 합니다.
    - paymentKey는 토스 결제 완료 후 콜백에서 자동으로 받아옵니다.
    - 토스 결제인 경우 서버에서 자동으로 결제 페이지로 리다이렉트됩니다 (프론트엔드 처리 불필요).
    - 예치금 결제(DEPOSIT)인 경우는 주문 생성 시 바로 결제가 처리되어 JSON 응답이 반환됩니다.
    
    ====================================================================================
    */
    @PostMapping("/cartItems")
    // TODO 토스 보완 결제 시 paymentKey 필수 여부와 검증 로직 추가 필요
    public Object createCartItemOrder(
            @Valid @RequestBody OrderCartItemRequest request
    ) {
        OrderCreateResponse response = orderService.createCartItemOrder(request);

        // 토스 결제인 경우 결제 페이지로 자동 리다이렉트
        if (request.paidType() == PaidType.TOSS_PAYMENT) {
            String orderName = "주문번호: " + response.orderCode();
            String redirectUrl = String.format("/orders/payment?orderId=%d&orderName=%s&amount=%d",
                    response.id(),
                    URLEncoder.encode(orderName, StandardCharsets.UTF_8),
                    response.totalPrice());
            return new RedirectView(redirectUrl);
        }

        // 예치금 결제 등은 기존대로 JSON 응답
        return BaseResponse.created(response);
    }

    /*
     * 직접 주문 생성 API
     * 
     * [사용 예시]
     * POST /api/orders/direct
     * 
     * [토스 결제 사용 시]
     * 1. 이 API로 주문 생성 (paidType: TOSS_PAYMENT, paymentKey: null)
     * 2. 서버에서 자동으로 결제 페이지로 리다이렉트
     * 3. 결제 페이지에서 토스 결제 진행
     * 4. 결제 완료 후 자동으로 콜백 처리됨
     * 
     * [예치금 결제 사용 시]
     * 1. 이 API로 주문 생성 (paidType: DEPOSIT)
     * 2. 주문 생성과 동시에 결제 처리 완료
     * 3. JSON 응답 반환 (orderStatus: COMPLETED)
     */
    @PostMapping("/direct")
    public Object createDirectOrder(
            @Valid @RequestBody OrderDirectRequest request
    ) {
        OrderCreateResponse response = orderService.createDirectOrder(request);

        // 토스 결제인 경우 결제 페이지로 자동 리다이렉트
        if (request.paidType() == PaidType.TOSS_PAYMENT) {
            String orderName = "주문번호: " + response.orderCode();
            String redirectUrl = String.format("/orders/payment?orderId=%d&orderName=%s&amount=%d",
                    response.id(),
                    URLEncoder.encode(orderName, StandardCharsets.UTF_8),
                    response.totalPrice());
            return new RedirectView(redirectUrl);
        }

        // 예치금 결제 등은 기존대로 JSON 응답
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

    /*
     * 결제 성공 콜백 처리
     * 
     * [설명]
     * 토스 결제가 성공적으로 완료되면 토스 서버에서 이 엔드포인트로 자동 리다이렉트됩니다.
     * paymentKey를 받아서 주문 결제를 완료 처리합니다.
     * 
     * [호출 방식]
     * 토스 서버에서 자동 호출 (프론트엔드에서 직접 호출하지 않음)
     * 
     * [파라미터]
     * - paymentKey: 토스에서 발급한 결제 키 (String)
     * - orderId: 주문 ID (String, "ORDER-1" 형식)
     * - amount: 결제 금액 (String)
     * 
     * [처리 과정]
     * 1. orderId에서 "ORDER-" 접두사 제거 후 Long 타입으로 변환
     * 2. 주문 조회 및 상태 확인 (CREATED 상태여야 함)
     * 3. PaymentService에 결제 승인 요청
     * 4. 주문 상태를 COMPLETED로 변경
     * 5. 주문 완료 이벤트 발행
     * 6. 성공 페이지로 리다이렉트
     * 
     * [예시 URL]
     * GET /api/orders/payment/success?paymentKey=tgen_test_xxx&orderId=ORDER-1&amount=10000
     */
    @GetMapping("/payment/success")
    public RedirectView paymentSuccess(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam String amount
    ) {
        try {
            Long orderIdLong = Long.parseLong(orderId.replace("ORDER-", ""));
            
            // 주문 조회 및 결제 처리
            orderService.completePaymentWithKey(orderIdLong, paymentKey);
            
            return new RedirectView("/orders/payment/success-page?orderId=" + orderId + "&amount=" + amount);
        } catch (Exception e) {
            return new RedirectView("/orders/payment/fail-page?error=" + e.getMessage());
        }
    }

    /*
     * 결제 실패 콜백 처리
     * 
     * [설명]
     * 토스 결제가 실패하거나 사용자가 결제를 취소하면 토스 서버에서 이 엔드포인트로 자동 리다이렉트됩니다.
     * 
     * [호출 방식]
     * 토스 서버에서 자동 호출 (프론트엔드에서 직접 호출하지 않음)
     * 
     * [파라미터]
     * - errorCode: 에러 코드 (선택)
     * - errorMessage: 에러 메시지 (선택)
     * - orderId: 주문 ID (선택)
     * 
     * [예시 URL]
     * GET /api/orders/payment/fail?errorCode=PAYMENT_FAILED&errorMessage=결제실패&orderId=ORDER-1
     */
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
