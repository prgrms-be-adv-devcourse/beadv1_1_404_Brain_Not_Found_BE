package com.ll.cart.client;

import com.ll.core.model.response.BaseResponse;
import com.ll.user.model.vo.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestClient restClient;

    @Value("${external.user-service.url:http://localhost:8083}")
    private String userServiceUrl;

    public UserResponse getUserByCode(String userCode) {
        BaseResponse<Map<String, Object>> response = restClient.get()
                .uri(userServiceUrl + "/api/users/info")
                .header("X-User-Code", userCode)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        
        if (response == null || response.getData() == null) {
            return null;
        }
        
        return UserResponse.from(response.getData());
    }
}