package com.ll.order.domain.client;

import com.ll.order.domain.model.vo.response.CartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class CartServiceClient {

    private final RestTemplate restTemplate;
    
    @Value("${external.cart-service.url:http://localhost:8083}")
    private String cartServiceUrl;

    public CartResponse getCartByCode(String cartCode) {
        String url = cartServiceUrl + "/api/carts/" + cartCode;
        return restTemplate.getForObject(url, CartResponse.class);
    }
}

