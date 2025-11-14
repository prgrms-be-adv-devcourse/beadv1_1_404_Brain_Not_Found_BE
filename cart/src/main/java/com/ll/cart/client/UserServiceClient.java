package com.ll.cart.client;

import com.ll.cart.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestClient restClient;

    @Value("${external.user-service.url:http://localhost:8081}")
    private String userServiceUrl;

    public UserResponse getUserByCode(String userCode) {
        return restClient.get()
                .uri(userServiceUrl + "/api/users/info")
                .header("userCode", userCode)
                .retrieve()
                .body(UserResponse.class);
    }
}