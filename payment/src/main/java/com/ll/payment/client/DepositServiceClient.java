package com.ll.payment.client;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.response.BaseResponse;
import com.ll.payment.exception.PaymentErrorCode;
import com.ll.payment.model.vo.request.DepositTransactionRequest;
import com.ll.payment.model.vo.response.DepositInfoResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class DepositServiceClient {

    private final RestClient restClient;

    @Value("${external.deposit-service.url:http://localhost:8086}")
    private String depositServiceUrl;

    @CircuitBreaker(name = "depositService", fallbackMethod = "getDepositInfoFallback")
    @Retry(name = "depositService")
    public DepositInfoResponse getDepositInfo(String userCode) {
        String url = depositServiceUrl + "/api/deposits?userCode=" + userCode;
        log.info("예치금 조회 요청 - userCode: {}", userCode);
        
        BaseResponse<DepositInfoResponse> response = restClient.get()
                .uri(url)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        
        if (response != null && response.getData() != null) {
            log.info("예치금 조회 성공 - userCode: {}, balance: {}", userCode, response.getData().balance());
            return response.getData();
        } else {
            log.warn("예치금 조회 결과 없음 - userCode: {}", userCode);
            return null;
        }
    }

    @CircuitBreaker(name = "depositService", fallbackMethod = "chargeDepositFallback")
    @Retry(name = "depositService")
    public void chargeDeposit(String userCode, Long amount, String referenceCode) {
        String url = depositServiceUrl + "/api/deposits/charge";
        log.info("예치금 충전 요청 - userCode: {}, amount: {}, referenceCode: {}", userCode, amount, referenceCode);
        
        restClient.post()
                .uri(url)
                .header("X-User-Code", userCode)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new DepositTransactionRequest(amount, referenceCode))
                .retrieve()
                .toBodilessEntity();
        log.info("예치금 충전 성공 - userCode: {}, amount: {}", userCode, amount);
    }

    @Deprecated
    public void deposit(String userCode, int amount, String referenceCode) {
        chargeDeposit(userCode, (long) amount, referenceCode);
    }

    // 출금
    @CircuitBreaker(name = "depositService", fallbackMethod = "withdrawFallback")
    @Retry(name = "depositService")
    public void withdraw(String userCode, Long amount, String referenceCode) {
        String url = depositServiceUrl + "/api/deposits/withdraw";
        log.info("예치금 출금 요청 - userCode: {}, amount: {}, referenceCode: {}", userCode, amount, referenceCode);
        
        restClient.post()
                .uri(url)
                .header("X-User-Code", userCode)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new DepositTransactionRequest(amount, referenceCode))
                .retrieve()
                .toBodilessEntity();
        log.info("예치금 출금 성공 - userCode: {}, amount: {}", userCode, amount);
    }

    // --- Resilience4j Fallback 메서드 (런타임에 자동 호출됨) ---
    private DepositInfoResponse getDepositInfoFallback(String userCode, Throwable e) {
        log.error("예치금 조회 실패 (재시도 모두 실패) - userCode: {}, error: {}", userCode, extractMessage(e), e);
        return null; // 예치금 조회 실패는 null 반환 (기존 동작 유지)
    }

    private void chargeDepositFallback(String userCode, Long amount, String referenceCode, Throwable e) {
        log.error("예치금 충전 실패 (재시도 모두 실패) - userCode: {}, amount: {}, referenceCode: {}, error: {}",
                userCode, amount, referenceCode, extractMessage(e), e);
        throw new BaseException(PaymentErrorCode.TOSS_PAYMENT_CREATE_FAILED,
                "예치금 충전 실패: " + extractMessage(e));
    }

    private void withdrawFallback(String userCode, Long amount, String referenceCode, Throwable e) {
        log.error("예치금 출금 실패 (재시도 모두 실패) - userCode: {}, amount: {}, referenceCode: {}, error: {}",
                userCode, amount, referenceCode, extractMessage(e), e);
        throw new BaseException(PaymentErrorCode.TOSS_PAYMENT_CREATE_FAILED,
                "예치금 출금 실패: " + extractMessage(e));
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

