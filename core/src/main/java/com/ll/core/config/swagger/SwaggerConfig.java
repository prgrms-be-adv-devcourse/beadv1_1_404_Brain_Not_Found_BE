package com.ll.core.config.swagger;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableConfigurationProperties(SwaggerProperties.class)
@RequiredArgsConstructor
@Profile("!test & !ci-test") // 테스트 프로파일에서는 SwaggerConfig 비활성화
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

    @Bean
    public OperationCustomizer hideUserCodeHeader() {
        return (operation, handlerMethod) -> {
            List<Parameter> original = operation.getParameters();

            if (original == null) {
                original = new ArrayList<>();
            }

            Parameter target = original.stream()
                    .filter(p -> "X-User-Code".equalsIgnoreCase(p.getName()))
                    .findFirst()
                    .orElse(null);

            if (target != null) {
                target.in(ParameterIn.HEADER.toString());
                target.setRequired(false);
                target.setName("X-User-Code");
                target.setDescription("사용자 고유 코드");
            }

            operation.setParameters(original);

            return operation;
        };
    }

    @Bean
    public OperationCustomizer hideUserCodeRole() {
        return (operation, handlerMethod) -> {
            List<Parameter> original = operation.getParameters();

            if (original == null) {
                original = new ArrayList<>();
            }

            Parameter target = original.stream()
                    .filter(p -> "X-Role".equalsIgnoreCase(p.getName()))
                    .findFirst()
                    .orElse(null);

            if (target != null) {
                target.in(ParameterIn.HEADER.toString());
                target.setRequired(false);
                target.setName("X-Role");
                target.setDescription("사용자 권한 정보");
            }

            operation.setParameters(original);

            return operation;
        };
    }
}

