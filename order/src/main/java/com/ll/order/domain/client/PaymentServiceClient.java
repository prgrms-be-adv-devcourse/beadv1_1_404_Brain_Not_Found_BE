package com.ll.order.domain.client;

import com.ll.core.model.exception.BaseException;
import com.ll.order.domain.exception.OrderErrorCode;
import com.ll.order.domain.model.vo.request.OrderPaymentRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentServiceClient {

    private final RestClient restClient;

    @Value("${external.payment-service.url:http://localhost:8087}")
    private String paymentServiceUrl;

    /**
     * 예치금 결제 요청
     * - Resilience4j Retry + CircuitBreaker 적용
     * - 네트워크/5xx 오류 시 최대 n회 재시도 (설정 기반)
     * - 실패율이 높아지면 Circuit Open 상태에서 빠른 실패
     */
    @CircuitBreaker(name = "paymentService", fallbackMethod = "depositPaymentFallback")
    @Retry(name = "paymentService")
    public String requestDepositPayment(OrderPaymentRequest request) {
        String url = paymentServiceUrl + "/api/payments/deposit";
        return restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(String.class);
    }

    @CircuitBreaker(name = "paymentService", fallbackMethod = "tossPaymentFallback")
    @Retry(name = "paymentService")
    public void requestTossPayment(OrderPaymentRequest request) {
        String url = paymentServiceUrl + "/api/payments/toss";
        restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(String.class);
    }

    @CircuitBreaker(name = "paymentService", fallbackMethod = "refundFallback")
    @Retry(name = "paymentService")
    public void requestRefund(Long orderId, String orderCode, String buyerCode, Integer refundAmount, String reason) {
        String url = paymentServiceUrl + "/api/payments/refund";
        Map<String, Object> request = new HashMap<>();
        request.put("orderId", orderId);
        request.put("orderCode", orderCode);
        request.put("buyerCode", buyerCode);
        request.put("refundAmount", refundAmount);
        request.put("reason", reason != null ? reason : "주문 취소");

        restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(String.class);
    }

    // --- Resilience4j Fallback 메서드들 ---

    // requestDepositPayment 실패 시 (Retry + CircuitBreaker 모두 포기한 상태)
    private String depositPaymentFallback(OrderPaymentRequest request, Throwable e) {
        throw new BaseException(OrderErrorCode.PAYMENT_PROCESSING_FAILED,
                "예치금 결제 요청 실패: " + extractMessage(e));
    }

    // requestTossPayment 실패 시
    private void tossPaymentFallback(OrderPaymentRequest request, Throwable e) {
        throw new BaseException(OrderErrorCode.PAYMENT_PROCESSING_FAILED,
                "토스 결제 요청 실패: " + extractMessage(e));
    }

    // requestRefund 실패 시
    private void refundFallback(Long orderId, String orderCode, String buyerCode,
                                Integer refundAmount, String reason, Throwable e) {
        throw new BaseException(OrderErrorCode.PAYMENT_PROCESSING_FAILED,
                "환불 요청 실패: " + extractMessage(e));
    }

    private String extractMessage(Throwable e) {
        if (e instanceof HttpClientErrorException http4xx) {
            HttpStatusCode status = http4xx.getStatusCode();
            return "status=" + status + ", body=" + http4xx.getResponseBodyAsString();
        }
        if (e instanceof HttpServerErrorException http5xx) {
            HttpStatusCode status = http5xx.getStatusCode();
            return "status=" + status + ", body=" + http5xx.getResponseBodyAsString();
        }
        return e.getMessage();
    }
}

