package com.ll.order.domain.client;

import com.ll.order.domain.model.vo.response.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class ProductServiceClient {

    private final RestTemplate restTemplate;

    @Value("${external.user-service.url:http://localhost:8081}")
    private String productServiceUrl;

    public ProductResponse getProductByCode(String productCode) {
        String url = productServiceUrl + "/api/products/" + productCode;
        return restTemplate.getForObject(url, ProductResponse.class);
    }

    public ProductResponse getProductById(Long productId) {
        return null;
    }
}
