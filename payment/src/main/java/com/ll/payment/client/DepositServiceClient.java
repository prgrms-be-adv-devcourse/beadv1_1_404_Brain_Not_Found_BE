package com.ll.payment.client;

import com.ll.core.model.response.BaseResponse;
import com.ll.payment.model.vo.request.DepositTransactionRequest;
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

    @Value("${external.deposit-service.url:http://localhost:8086}")
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

    public void chargeDeposit(String userCode, Long amount, String referenceCode) {
        String url = depositServiceUrl + "/api/deposits/charge";
        log.info("예치금 충전 요청 - userCode: {}, amount: {}, referenceCode: {}", userCode, amount, referenceCode);
        
        try {
            restClient.post()
                    .uri(url)
                    .header("X-User-Code", userCode)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new DepositTransactionRequest(amount, referenceCode))
                    .retrieve()
                    .toBodilessEntity();
            log.info("예치금 충전 성공 - userCode: {}, amount: {}", userCode, amount);
        } catch (Exception e) {
            log.error("예치금 충전 실패 - userCode: {}, amount: {}, error: {}", userCode, amount, e.getMessage(), e);
            throw e;
        }
    }

    @Deprecated
    public void deposit(String userCode, int amount, String referenceCode) {
        chargeDeposit(userCode, (long) amount, referenceCode);
    }

    // 출금
    public void withdraw(String userCode, Long amount, String referenceCode) {
        String url = depositServiceUrl + "/api/deposits/withdraw";
        log.info("예치금 출금 요청 - userCode: {}, amount: {}, referenceCode: {}", userCode, amount, referenceCode);
        
        try {
            restClient.post()
                    .uri(url)
                    .header("X-User-Code", userCode)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new DepositTransactionRequest(amount, referenceCode))
                    .retrieve()
                    .toBodilessEntity();
            log.info("예치금 출금 성공 - userCode: {}, amount: {}", userCode, amount);
        } catch (Exception e) {
            log.error("예치금 출금 실패 - userCode: {}, amount: {}, error: {}", userCode, amount, e.getMessage(), e);
            throw e;
        }
    }
}

