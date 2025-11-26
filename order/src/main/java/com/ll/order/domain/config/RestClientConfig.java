package com.ll.order.domain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    //RestTemplate 보다 고도화된 RestClient 추천드립니다.
    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder.build();
    }
}


