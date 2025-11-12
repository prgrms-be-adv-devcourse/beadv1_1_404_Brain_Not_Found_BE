package com.ll.products.global.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.ll.products.global.client.dto.UserResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserClient {

    private final RestClient userRestClient;

    public String getSellerName(Long sellerId) {
        try {
            UserResponse response = userRestClient.get()
                    .uri("/api/users/info")
                    .header("sellerId", sellerId.toString())
                    .retrieve()
                    .body(UserResponse.class);
            return response != null ? response.name() : null;
        } catch (Exception e) {
            log.error("해당하는 id의 판매자를 찾을 수 없습니다 : {}", sellerId, e);
            return null;
        }
    }
}