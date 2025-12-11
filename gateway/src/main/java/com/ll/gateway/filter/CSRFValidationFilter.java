package com.ll.gateway.filter;

import com.ll.gateway.exception.GatewayBaseException;
import com.ll.gateway.exception.GatewayErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class CSRFValidationFilter extends AbstractGatewayFilterFactory<CSRFValidationFilter.Config> {

    public CSRFValidationFilter() {
        super(Config.class);
    }

    public static class Config {
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();

            String method = request.getMethod().name();
            if ("POST".equalsIgnoreCase(method) ||
                    "PUT".equalsIgnoreCase(method) ||
                    "DELETE".equalsIgnoreCase(method) ||
                    "PATCH".equalsIgnoreCase(method)) {

                String csrfHeader = request.getHeaders().getFirst("X-CSRF-TOKEN");
                HttpCookie csrfCookie = request.getCookies().getFirst("CSRF-TOKEN");
                String csrfCookieValue = (csrfCookie != null) ? csrfCookie.getValue() : null;

                if (csrfHeader == null || !csrfHeader.equals(csrfCookieValue)) {
                    return Mono.error(new GatewayBaseException(GatewayErrorCode.FORBIDDEN));
                }
            }

            return chain.filter(exchange);
        };
    }

}
