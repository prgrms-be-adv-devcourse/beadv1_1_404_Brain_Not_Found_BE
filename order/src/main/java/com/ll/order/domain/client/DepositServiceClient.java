package com.ll.order.domain.client;

import com.ll.core.model.exception.BaseException;
import com.ll.order.domain.exception.OrderErrorCode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepositServiceClient {

    private final RestClient restClient;

    @Value("${external.payment-service.url:http://localhost:8087}")
    private String paymentServiceUrl;

    @CircuitBreaker(name = "paymentService", fallbackMethod = "chargeDepositFallback")
    @Retry(name = "paymentService")
    public void chargeDeposit(String userCode, Long amount, String referenceCode) {
        String url = paymentServiceUrl + "/api/deposits/charge";
        Map<String, Object> request = Map.of(
                "amount", amount,
                "referenceCode", referenceCode
        );
        
        restClient.post()
                .uri(url)
                .header("X-User-Code", userCode)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
        
        log.debug("예치금 충전 API 호출 완료 - userCode: {}, amount: {}, referenceCode: {}", 
                userCode, amount, referenceCode);
    }

    // chargeDeposit 실패 시
    private void chargeDepositFallback(String userCode, Long amount, String referenceCode, Throwable e) {
        log.error("예치금 충전 실패 (재시도 모두 실패) - userCode: {}, amount: {}, referenceCode: {}, error: {}",
                userCode, amount, referenceCode, extractMessage(e), e);
        throw new BaseException(OrderErrorCode.PAYMENT_PROCESSING_FAILED,
                "예치금 충전 실패: " + extractMessage(e));
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

