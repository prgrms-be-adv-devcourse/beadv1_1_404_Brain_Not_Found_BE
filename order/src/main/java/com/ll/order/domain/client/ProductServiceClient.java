package com.ll.order.domain.client;

import com.ll.core.model.response.BaseResponse;
import com.ll.order.domain.model.vo.response.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class ProductServiceClient {

    private final RestClient restClient;

    @Value("${external.product-service.url:http://localhost:8081}")
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

    public ProductResponse getProductById(Long productId) {
        return null;
    }
}
