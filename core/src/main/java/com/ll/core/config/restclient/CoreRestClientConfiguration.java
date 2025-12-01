package com.ll.core.config.restclient;

import com.ll.core.tracing.CorrelationContext;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreRestClientConfiguration {
    @Bean
    public RestClientCustomizer correlationIdAutoInjector() {
        return builder -> builder.requestInitializer( request ->
                request.getHeaders().add("X-Correlation-Id", CorrelationContext.get())
        );
    }
}
