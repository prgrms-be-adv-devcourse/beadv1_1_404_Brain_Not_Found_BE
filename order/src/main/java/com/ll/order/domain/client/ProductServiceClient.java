package com.ll.order.domain.client;

import com.ll.core.model.response.BaseResponse;
import com.ll.order.domain.model.vo.response.product.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductServiceClient {

    private final RestClient restClient;

    @Value("${external.product-service.url:http://localhost:8085}")
    private String productServiceUrl;

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

    public void decreaseInventory(String productCode, Integer quantity) {
        log.info("재고 차감 요청 - productCode: {}, quantity: {}", productCode, quantity);
        restClient.patch()
                .uri(productServiceUrl + "/api/products/{productCode}/inventory", productCode)
                .body(Map.of("quantity", -quantity)) // 음수로 전달하여 차감
                .retrieve()
                .toBodilessEntity();
    }
}
