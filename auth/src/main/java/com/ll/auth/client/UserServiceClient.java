package com.ll.auth.client;

import com.ll.core.model.response.BaseResponse;
import com.ll.auth.model.vo.request.UserLoginRequest;
import com.ll.auth.model.vo.response.UserLoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestClient restClient;

    @Value("${external.user-service.url:http://localhost:8081}")
    private String userServiceUrl;

    public BaseResponse<UserLoginResponse> requestUserLogin(UserLoginRequest userLoginRequest) {
        return restClient.post()
                .uri(userServiceUrl + "/api/users")
                .body(userLoginRequest)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<BaseResponse<UserLoginResponse>>() {})
                .getBody();
    }

    public BaseResponse<UserLoginResponse> requestUserInfo(String userCode) {
        return restClient.post()
                .uri(userServiceUrl + "/api/users")
                .header("X-User-Code",userCode)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<BaseResponse<UserLoginResponse>>() {})
                .getBody();
    }
}
