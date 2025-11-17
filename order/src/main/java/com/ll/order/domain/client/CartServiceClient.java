package com.ll.order.domain.client;

import com.ll.core.model.response.BaseResponse;
import com.ll.order.domain.model.vo.response.CartItemsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class CartServiceClient {

    private final RestClient restClient;
    
    @Value("${external.cart-service.url:http://localhost:8083}")
    private String cartServiceUrl;

    public CartItemsResponse getCartByCode(String cartCode) {
        // cartCode 파라미터는 실제로 userCode로 사용됩니다
        BaseResponse<CartItemsResponse> response = restClient.get()
                .uri(cartServiceUrl + "/api/carts/cartItems")
                .header("X-User-Code", cartCode)
                .retrieve()
                .body(new ParameterizedTypeReference<BaseResponse<CartItemsResponse>>() {});

        if (response == null || response.getData() == null) {
            return null;
        }

        return response.getData();
    }
}

