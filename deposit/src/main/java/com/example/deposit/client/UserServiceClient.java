package com.example.deposit.client;

import com.example.deposit.model.vo.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestClient restClient;

    @Value("${external.user-service.url:http://localhost:8081}" )
    private String userServiceUrl;

    /**
     * 회원 서비스에서 userCode로 사용자 정보를 조회합니다.
     * @param userCode 사용자 코드
     * @return 사용자 정보 (userId, userCode)
     */
    public UserInfoResponse getUserByCode(String userCode) {
        return new UserInfoResponse(1L, "userCode123"); // TODO: 임시 구현, 실제로는 RestClient 를 사용하여 회원 서비스와 통신해야 함
//        return restClient.get()
//                .uri(userServiceUrl + "/api/users/{userCode}", userCode)
//                .retrieve()
//                .body(UserInfoResponse.class);
    }

}
