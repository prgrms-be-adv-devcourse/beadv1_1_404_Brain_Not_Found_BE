package com.example.deposit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfiguration {

    @Bean
    public RestClient restTemplate() {
        return RestClient.builder()
                // 추가적인 설정이 필요하면 여기서 설정
                .build();
    }

}
