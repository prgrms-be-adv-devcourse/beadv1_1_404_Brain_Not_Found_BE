package com.ll.payment.client;

import com.ll.core.model.response.BaseResponse;
import com.ll.payment.model.dto.DepositInfoResponse;
import com.ll.payment.model.dto.DepositWithdrawRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class DepositServiceClient {

    private final RestClient restClient;

    @Value("${external.deposit-service.url:http://localhost:8085}")
    private String depositServiceUrl;

    public DepositInfoResponse getDepositInfo(String userCode) {
        String url = depositServiceUrl + "/api/deposits";
        BaseResponse<DepositInfoResponse> response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(url)
                        .queryParam("userCode", userCode)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        return response != null ? response.getData() : null;
    }

    public void deposit(String userCode, int amount, String referenceCode) {
        String url = depositServiceUrl + "/api/deposits/deposit";
        restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(url)
                        .queryParam("userCode", userCode)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new DepositWithdrawRequest(amount, referenceCode))
                .retrieve()
                .toBodilessEntity();
    }

    public void withdraw(String userCode, int amount, String referenceCode) {
        String url = depositServiceUrl + "/api/deposits/withdraw";
        restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(url)
                        .queryParam("userCode", userCode)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new DepositWithdrawRequest(amount, referenceCode))
                .retrieve()
                .toBodilessEntity();
    }
}

