package com.ll.cart.client;

import com.ll.core.model.response.BaseResponse;
import com.ll.user.model.enums.AccountStatus;
import com.ll.user.model.enums.Grade;
import com.ll.user.model.enums.Role;
import com.ll.user.model.enums.SocialProvider;
import com.ll.user.model.vo.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
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
        
        Map<String, Object> userData = response.getData();
        return new UserResponse(
                userData.get("id") != null ? Long.valueOf(userData.get("id").toString()) : null,
                userData.get("code") != null ? userData.get("code").toString() : null,
                userData.get("socialId") != null ? userData.get("socialId").toString() : null,
                userData.get("socialProvider") != null ? SocialProvider.valueOf(userData.get("socialProvider").toString()) : null,
                userData.get("email") != null ? userData.get("email").toString() : null,
                userData.get("name") != null ? userData.get("name").toString() : null,
                userData.get("role") != null ? Role.valueOf(userData.get("role").toString()) : null,
                userData.get("profileImageUrl") != null ? userData.get("profileImageUrl").toString() : null,
                userData.get("mannerScore") != null ? Long.valueOf(userData.get("mannerScore").toString()) : null,
                userData.get("grade") != null ? Grade.valueOf(userData.get("grade").toString()) : null,
                userData.get("accountStatus") != null ? AccountStatus.valueOf(userData.get("accountStatus").toString()) : null,
                userData.get("accountBank") != null ? userData.get("accountBank").toString() : null,
                userData.get("accountNumber") != null ? userData.get("accountNumber").toString() : null,
                userData.get("createAt") != null ? LocalDateTime.parse(userData.get("createAt").toString()) : null,
                userData.get("updatedAt") != null ? LocalDateTime.parse(userData.get("updatedAt").toString()) : null
        );
    }
}