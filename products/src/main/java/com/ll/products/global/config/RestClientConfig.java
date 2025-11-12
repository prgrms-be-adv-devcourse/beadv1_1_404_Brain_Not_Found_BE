package com.ll.products.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${api.user.base-url}")
    private String userBaseUrl;

    @Bean
    public RestClient userRestClient() {
        return RestClient.builder()
                .baseUrl(userBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}