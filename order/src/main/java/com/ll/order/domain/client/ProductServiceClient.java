package com.ll.order.domain.client;

import com.ll.core.model.response.BaseResponse;
import com.ll.order.domain.model.vo.response.product.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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

    /**
     * 재고 복구 요청
     * @param productCode 상품 코드
     * @param quantity 복구할 수량
     */
    public void restoreInventory(String productCode, Integer quantity) {
        // TODO: products 서비스에 재고 복구 API 엔드포인트 추가 필요
        // 예: POST /api/products/{productCode}/inventory/restore
        // 또는 PUT /api/products/{productCode}/inventory?action=restore&quantity={quantity}
        log.warn("재고 복구 API가 아직 구현되지 않았습니다. productCode: {}, quantity: {}", productCode, quantity);
        // 임시로 주석 처리
        // restClient.post()
        //         .uri(productServiceUrl + "/api/products/{productCode}/inventory/restore", productCode)
        //         .body(Map.of("quantity", quantity))
        //         .retrieve()
        //         .toBodilessEntity();
    }
}
