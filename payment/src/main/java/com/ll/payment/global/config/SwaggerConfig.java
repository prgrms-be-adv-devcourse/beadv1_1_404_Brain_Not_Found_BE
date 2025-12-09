package com.ll.payment.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(
                        List.of(new Server().url("http://localhost:8000"), // 게이트웨이 테스트 Server localhost:8000/swagger-ui.html
                                new Server().url("http://localhost:8087")))    // 로컬 테스트 Server localhost:8087/swagger-ui.html
                .info(
                        new Info().title("Gooream").description("Gooream API 명세서")
                );
    }
}
