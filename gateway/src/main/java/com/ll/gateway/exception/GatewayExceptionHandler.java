package com.ll.gateway.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.gateway.resopnse.GatewayBaseResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.*;

@Component
@Order(-2)
@RequiredArgsConstructor
public class GatewayExceptionHandler implements WebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public @NonNull reactor.core.publisher.Mono<Void> handle(ServerWebExchange exchange, @NonNull Throwable ex) {

        if (exchange.getResponse().isCommitted()) {
            return reactor.core.publisher.Mono.error(ex);
        }

        // 401 예외만 처리
        if (ex instanceof GatewayBaseException gatewayEx &&
                gatewayEx.getErrorCode() == GatewayErrorCode.UNAUTHORIZED) {
            return writeUnauthorized(exchange);
        }

        return reactor.core.publisher.Mono.error(ex);
    }

    private reactor.core.publisher.Mono<Void> writeUnauthorized(ServerWebExchange exchange) {

        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        GatewayBaseResponse<Object> body = GatewayBaseResponse.error(GatewayErrorCode.UNAUTHORIZED);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (Exception e) {
            return reactor.core.publisher.Mono.error(e);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(reactor.core.publisher.Mono.just(buffer));
    }
}
