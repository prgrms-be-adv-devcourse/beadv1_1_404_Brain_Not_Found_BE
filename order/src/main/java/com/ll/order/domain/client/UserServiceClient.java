package com.ll.order.domain.client;

import com.ll.order.domain.model.vo.response.ClientResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestTemplate restTemplate;
    
    @Value("${external.user-service.url:http://localhost:8081}")
    private String userServiceUrl;

    /**
     * 회원 서비스에서 userCode로 사용자 정보를 조회합니다.
     * @param userCode 사용자 코드
     * @return 사용자 정보 (id, name, address)
     */
    public ClientResponse getUserByCode(String userCode) {
        String url = userServiceUrl + "/api/users/" + userCode;
        return restTemplate.getForObject(url, ClientResponse.class);
    }
}

