package com.ll.order.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    //RestTemplate 보다 고도화된 RestClient 추천드립니다.
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

