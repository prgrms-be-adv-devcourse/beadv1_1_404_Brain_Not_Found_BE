package com.ll.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = exchange.getRequest().getId();
        String path = exchange.getRequest().getURI().getPath();
        long startTime = System.currentTimeMillis();

        log.info("Incoming request: [{}] {}", requestId, path);

        return chain.filter(exchange)
                .doOnError(ex -> log.error("Error processing request [{}]: {}", requestId, ex.getMessage()))
                .doFinally(signalType -> {
                    long duration = System.currentTimeMillis() - startTime;
                    int status = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value() : 0;
                    log.info("Completed request: [{}] {} Status: {} Duration: {}ms",
                            requestId, path, status, duration);
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
