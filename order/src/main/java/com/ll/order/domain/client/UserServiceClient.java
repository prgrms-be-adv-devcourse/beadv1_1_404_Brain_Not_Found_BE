package com.ll.order.domain.client;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.response.BaseResponse;
import com.ll.order.domain.exception.OrderErrorCode;
import com.ll.order.domain.model.vo.response.user.UserResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final RestClient restClient;
    
    @Value("${external.user-service.url:http://localhost:8083}")
    private String userServiceUrl;

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByCodeFallback")
    @Retry(name = "userService")
    public UserResponse getUserByCode(String userCode) {
        log.debug("userServiceUrl = {}", userServiceUrl);
        BaseResponse<UserResponse> response = restClient.get()
                .uri(userServiceUrl + "/api/users/info")
                .header("X-User-Code", userCode)
                .retrieve()
                .body(new ParameterizedTypeReference<BaseResponse<UserResponse>>() {});
        
        if (response == null || response.getData() == null) {
            return null;
        }
        
        return response.getData();
    }

    // --- Resilience4j Fallback 메서드 (런타임에 자동 호출됨) ---
    private UserResponse getUserByCodeFallback(String userCode, Throwable e) {
        log.error("사용자 조회 실패 (재시도 모두 실패) - userCode: {}, error: {}", userCode, extractMessage(e), e);
        throw new BaseException(OrderErrorCode.USER_NOT_FOUND,
                "사용자 조회 실패: " + extractMessage(e));
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

