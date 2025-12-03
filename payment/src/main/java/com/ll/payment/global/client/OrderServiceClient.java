package com.ll.payment.global.client;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.response.BaseResponse;
import com.ll.payment.payment.exception.PaymentErrorCode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component
public class OrderServiceClient {

    private final RestClient restClient;

    @Value("${external.order-service.url:http://localhost:8082}")
    private String orderServiceUrl;

    public OrderServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @CircuitBreaker(name = "orderService", fallbackMethod = "updateOrderStatusFallback")
    @Retry(name = "orderService")
    public void updateOrderStatus(String orderCode, String status) {
        if (orderCode == null || orderCode.isBlank()) {
            throw new IllegalArgumentException("주문 코드가 필요합니다.");
        }
        restClient.patch()
                .uri(orderServiceUrl + "/api/orders/{orderCode}/status", orderCode)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("status", status))
                .retrieve()
                .toBodilessEntity();
    }

    @CircuitBreaker(name = "orderService", fallbackMethod = "getOrderCodeByIdFallback")
    @Retry(name = "orderService")
    public String getOrderCodeById(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("주문 ID가 필요합니다.");
        }
        BaseResponse<Map<String, String>> response = restClient.get()
                .uri(orderServiceUrl + "/api/orders/{orderId}/code", orderId)
                .retrieve()
                .body(new ParameterizedTypeReference<BaseResponse<Map<String, String>>>() {});
        if (response != null && response.getData() != null) {
            return response.getData().get("orderCode");
        }
        throw new IllegalStateException("주문 코드 조회 응답이 비어있습니다. orderId: " + orderId);
    }

    // --- Resilience4j Fallback 메서드 (런타임에 자동 호출됨) ---
    private void updateOrderStatusFallback(String orderCode, String status, Throwable e) {
        log.error("주문 상태 업데이트 실패 (재시도 모두 실패) - orderCode: {}, status: {}, error: {}",
                orderCode, status, extractMessage(e), e);
        throw new BaseException(PaymentErrorCode.ORDER_SERVICE_NOTIFICATION_FAILED,
                "주문 상태 업데이트 실패: " + extractMessage(e));
    }

    private String getOrderCodeByIdFallback(Long orderId, Throwable e) {
        log.error("주문 코드 조회 실패 (재시도 모두 실패) - orderId: {}, error: {}", orderId, extractMessage(e), e);
        throw new BaseException(PaymentErrorCode.ORDER_SERVICE_NOTIFICATION_FAILED,
                "주문 코드 조회 실패: " + extractMessage(e));
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

