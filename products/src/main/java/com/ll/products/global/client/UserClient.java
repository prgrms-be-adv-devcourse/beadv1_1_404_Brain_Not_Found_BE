package com.ll.products.global.client;

import com.ll.core.model.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
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
            BaseResponse<UserResponse> response = userRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/users/info")
                            .queryParam("id", sellerId)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<BaseResponse<UserResponse>>() {});
            if (response != null && response.getData() != null) {
                return response.getData().name();
            }
            log.warn("판매자 정보 조회 실패 : {}", sellerId);
            return null;
        } catch (Exception e) {
            log.error("해당하는 id의 판매자를 찾을 수 없음 : {}",sellerId, e);
            return null;
        }
    }
}