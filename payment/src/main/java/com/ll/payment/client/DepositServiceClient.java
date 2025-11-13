package com.ll.payment.client;

import com.ll.payment.model.dto.DepositBalanceResponse;
import com.ll.payment.model.dto.DepositUseRequest;
import com.ll.payment.model.dto.DepositUseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class DepositServiceClient {

    private final RestClient restClient;

    @Value("${external.deposit-service.url:http://localhost:8085}")
    private String depositServiceUrl;

    public DepositBalanceResponse getBalance(String buyerCode) {
        String url = depositServiceUrl + "/api/deposits/" + buyerCode + "/balance";
        return restClient.get()
                .uri(url)
                .retrieve()
                .body(DepositBalanceResponse.class);
    }

    public DepositUseResponse useDeposit(DepositUseRequest request) {
        String url = depositServiceUrl + "/api/deposits/use";
        return restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(DepositUseResponse.class);
    }
}

