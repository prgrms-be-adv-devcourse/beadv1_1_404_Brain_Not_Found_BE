package com.ll.order.domain.client;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.response.BaseResponse;
import com.ll.order.domain.exception.OrderErrorCode;
import com.ll.order.domain.model.vo.response.product.ProductResponse;
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

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductServiceClient {

    private final RestClient restClient;

    @Value("${external.product-service.url:http://localhost:8085}")
    private String productServiceUrl;

    @CircuitBreaker(name = "productService", fallbackMethod = "getProductByCodeFallback")
    @Retry(name = "productService")
    public ProductResponse getProductByCode(String productCode) {
        BaseResponse<ProductResponse> response = restClient.get()
                .uri(productServiceUrl + "/api/products/{productCode}", productCode)
                .retrieve()
                .body(new ParameterizedTypeReference<BaseResponse<ProductResponse>>() {});
        
        if (response == null || response.getData() == null) {
            return null;
        }
        
        return response.getData();
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "decreaseInventoryFallback")
    @Retry(name = "productService")
    public void decreaseInventory(String productCode, Integer quantity) {
        log.info("재고 차감 요청 - productCode: {}, quantity: {}", productCode, quantity);
        restClient.patch()
                .uri(productServiceUrl + "/api/products/{productCode}/inventory", productCode)
                .body(Map.of("quantity", -quantity)) // 음수로 전달하여 차감
                .retrieve()
                .toBodilessEntity();
    }

    // --- Resilience4j Fallback 메서드 (런타임에 자동 호출됨) ---
    // getProductByCode 실패 시 (Retry + CircuitBreaker 모두 포기한 상태)
    private ProductResponse getProductByCodeFallback(String productCode, Throwable e) {
        log.error("상품 조회 실패 (재시도 모두 실패) - productCode: {}, error: {}", productCode, extractMessage(e), e);
        throw new BaseException(OrderErrorCode.PRODUCT_NOT_FOUND,
                "상품 조회 실패: " + extractMessage(e));
    }

    // decreaseInventory 실패 시
    private void decreaseInventoryFallback(String productCode, Integer quantity, Throwable e) {
        log.error("재고 차감 실패 (재시도 모두 실패) - productCode: {}, quantity: {}, error: {}",
                productCode, quantity, extractMessage(e), e);
        throw new BaseException(OrderErrorCode.INVENTORY_DEDUCTION_FAILED,
                "재고 차감 실패: " + extractMessage(e));
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
