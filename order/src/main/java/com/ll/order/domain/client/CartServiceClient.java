package com.ll.order.domain.client;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.response.BaseResponse;
import com.ll.order.domain.exception.OrderErrorCode;
import com.ll.order.domain.model.vo.response.cart.CartItemsResponse;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class CartServiceClient {

    private final RestClient restClient;
    
    @Value("${external.product-service.url:http://localhost:8085}")
    private String cartServiceUrl;

    @CircuitBreaker(name = "cartService", fallbackMethod = "getCartByCodeFallback")
    @Retry(name = "cartService")
    public CartItemsResponse getCartByCode(String userCode) {
        // cartCode 파라미터는 실제로 userCode로 사용됩니다
        BaseResponse<CartItemsResponse> response = restClient.get()
                .uri(cartServiceUrl + "/api/carts/cartItems")
                .header("X-User-Code", userCode)
                .retrieve()
                .body(new ParameterizedTypeReference<BaseResponse<CartItemsResponse>>() {});

        if (response == null || response.getData() == null) {
            return null;
        }

        return response.getData();
    }

    // --- Resilience4j Fallback 메서드 (런타임에 자동 호출됨) ---
    private CartItemsResponse getCartByCodeFallback(String cartCode, Throwable e) {
        log.error("장바구니 조회 실패 (재시도 모두 실패) - cartCode: {}, error: {}", cartCode, extractMessage(e), e);
        throw new BaseException(OrderErrorCode.CART_NOT_FOUND,
                "장바구니 조회 실패: " + extractMessage(e));
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

