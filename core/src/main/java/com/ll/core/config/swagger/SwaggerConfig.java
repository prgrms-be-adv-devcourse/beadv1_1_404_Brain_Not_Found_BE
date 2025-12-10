package com.ll.core.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties(SwaggerProperties.class)
@RequiredArgsConstructor
public class SwaggerConfig {

    private final SwaggerProperties properties;

    @Bean
    public OpenAPI customOpenAPI() {
        List<Server> serverList = properties.getServers().stream()
                .map(url -> new Server().url(url))
                .toList();

        return new OpenAPI()
                .servers(serverList)
                .info(new Info()
                        .title("Gooream")
                        .description("Gooream API 명세서"));
    }
}

