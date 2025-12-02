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

    public String getSellerName(String sellerCode) {
        try {
            BaseResponse<UserResponse> response = userRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/users/info")
                            .build())
                    .header("X-User-Code", sellerCode)
                    .retrieve()
                    .body(new ParameterizedTypeReference<BaseResponse<UserResponse>>() {});
            if (response != null && response.getData() != null) {
                return response.getData().name();
            }
            log.warn("판매자명 조회 실패 : {}", sellerCode);
            return null;
        } catch (Exception e) {
            log.error("판매자명 조회 중 예외 발생 : {}, error: {}", sellerCode, e.getMessage(), e);
            return null;
        }
    }
}