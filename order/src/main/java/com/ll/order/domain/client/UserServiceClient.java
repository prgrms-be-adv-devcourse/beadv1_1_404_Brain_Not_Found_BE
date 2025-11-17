package com.ll.order.domain.client;

import com.ll.core.model.response.BaseResponse;
import com.ll.order.domain.model.vo.response.ClientResponse;
import com.ll.order.domain.model.vo.response.UserResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;
    
    @Value("${external.user-service.url:http://localhost:8081}")
    private String userServiceUrl;

    /**
     * 회원 서비스에서 userCode로 사용자 정보를 조회합니다.
     * @param userCode 사용자 코드
     * @return 사용자 정보 (id, name, address)
     */
    public ClientResponse getUserByCode(String userCode) {
        BaseResponse<Map<String, Object>> response = restClient.get()
                .uri(userServiceUrl + "/api/users/info")
                .header("X-User-Code", userCode)
                .retrieve()
                .body(new ParameterizedTypeReference<BaseResponse<Map<String, Object>>>() {});
        
        if (response == null || response.getData() == null) {
            return null;
        }
        
        Map<String, Object> userData = response.getData();
        UserResponse userResponse = objectMapper.convertValue(userData, UserResponse.class);
        
        return new ClientResponse(
                userResponse.id(),
                userResponse.name(),
                userResponse.address()
        );
    }
}

