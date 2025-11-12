package com.ll.order.domain.client;

import com.ll.order.domain.model.vo.response.CartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class CartServiceClient {

    private final RestClient restClient;
    
    @Value("${external.cart-service.url:http://localhost:8083}")
    private String cartServiceUrl;

    public CartResponse getCartByCode(String cartCode) {
        return restClient.get()
                .uri(cartServiceUrl + "/api/carts/{cartCode}", cartCode)
                .retrieve()
                .body(CartResponse.class);
    }
}

