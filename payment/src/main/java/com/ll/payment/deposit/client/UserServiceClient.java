package com.ll.payment.deposit.client;

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

}
