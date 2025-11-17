package com.ll.payment.client;

import com.ll.core.model.response.BaseResponse;
import com.ll.payment.model.vo.request.DepositWithdrawRequest;
import com.ll.payment.model.vo.response.DepositInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class DepositServiceClient {

    private final RestClient restClient;

    @Value("${external.deposit-service.url:http://localhost:8085}")
    private String depositServiceUrl;

    public DepositInfoResponse getDepositInfo(String userCode) {
        String url = depositServiceUrl + "/api/deposits?userCode=" + userCode;
        log.info("예치금 조회 요청 - userCode: {}", userCode);
        
        try {
            BaseResponse<DepositInfoResponse> response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            
            if (response != null && response.getData() != null) {
                log.info("예치금 조회 성공 - userCode: {}, balance: {}", userCode, response.getData().balance());
                return response.getData();
            } else {
                log.warn("예치금 조회 결과 없음 - userCode: {}", userCode);
                return null;
            }
        } catch (Exception e) {
            log.error("예치금 조회 실패 - userCode: {}, error: {}", userCode, e.getMessage());
            return null;
        }
    }

    public void deposit(String userCode, int amount, String referenceCode) {
        String url = depositServiceUrl + "/api/deposits/deposit?userCode=" + userCode;
        restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new DepositWithdrawRequest(amount, referenceCode))
                .retrieve()
                .toBodilessEntity();
    }

    public void withdraw(String userCode, int amount, String referenceCode) {
        String url = depositServiceUrl + "/api/deposits/withdraw?userCode=" + userCode;
        restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new DepositWithdrawRequest(amount, referenceCode))
                .retrieve()
                .toBodilessEntity();
    }
}

