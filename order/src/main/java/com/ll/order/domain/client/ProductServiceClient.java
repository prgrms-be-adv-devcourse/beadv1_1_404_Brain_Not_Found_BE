package com.ll.order.domain.client;

import com.ll.order.domain.model.vo.response.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class ProductServiceClient {

    private final RestClient restClient;

    @Value("${external.user-service.url:http://localhost:8081}")
    private String productServiceUrl;

    public ProductResponse getProductByCode(String productCode) {
        return restClient.get()
                .uri(productServiceUrl + "/api/products/{productCode}", productCode)
                .retrieve()
                .body(ProductResponse.class);
    }

    public ProductResponse getProductById(Long productId) {
        return null;
    }
}
