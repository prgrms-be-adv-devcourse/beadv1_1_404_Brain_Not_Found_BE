package com.ll.order.domain.client;

import com.ll.core.model.exception.BaseException;
import com.ll.order.domain.exception.OrderErrorCode;
import com.ll.order.domain.model.vo.request.OrderPaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentServiceClient {

    private final RestClient restClient;

    @Value("${external.payment-service.url:http://localhost:8087}")
    private String paymentServiceUrl;

    @Retryable(
            retryFor = {
                    ResourceAccessException.class, // 타임아웃/연결 오류
                    HttpServerErrorException.class // 5xx 서버 오류
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 2.0)
    )
    public String requestDepositPayment(OrderPaymentRequest request) {
        String url = paymentServiceUrl + "/api/payments/deposit";
        return restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(String.class);
    }

    @Retryable(
            retryFor = {
                    ResourceAccessException.class,
                    HttpServerErrorException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 2.0)
    )
    public void requestTossPayment(OrderPaymentRequest request) {
        String url = paymentServiceUrl + "/api/payments/toss";
        restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(String.class);
    }

    @Retryable(
            retryFor = {
                    ResourceAccessException.class,
                    HttpServerErrorException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 2.0)
    )
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

    // --- Recover 메서드들 ---
    @Recover
    public String recoverDepositPayment(Exception e, OrderPaymentRequest request) {
        // 4xx (HttpClientErrorException) 또는 BaseException 등은 Retry 대상이 아니므로 여기까지 안 옴
        // 네트워크/서버 오류를 3번 시도해도 실패한 경우 최종 예외로 변환
        throw new BaseException(OrderErrorCode.PAYMENT_PROCESSING_FAILED,
                "예치금 결제 요청 실패: " + extractMessage(e));
    }

    @Recover
    public void recoverTossPayment(Exception e, OrderPaymentRequest request) {
        throw new BaseException(OrderErrorCode.PAYMENT_PROCESSING_FAILED,
                "토스 결제 요청 실패: " + extractMessage(e));
    }

    @Recover
    public void recoverRefund(Exception e, Long orderId, String orderCode, String buyerCode, Integer refundAmount, String reason) {
        throw new BaseException(OrderErrorCode.PAYMENT_PROCESSING_FAILED,
                "환불 요청 실패: " + extractMessage(e));
    }

    private String extractMessage(Exception e) {
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

