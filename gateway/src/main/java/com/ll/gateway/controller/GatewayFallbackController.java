package com.ll.gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.gateway.exception.GatewayErrorCode;
import com.ll.gateway.resopnse.GatewayBaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fallback")
@RequiredArgsConstructor
public class GatewayFallbackController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @RequestMapping(value = "/product", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public Mono<Void> productFallback(ServerWebExchange exchange) {
        return writeResponse(exchange, GatewayErrorCode.FORBIDDEN);
    }

    @RequestMapping(value = "/order", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public Mono<Void> orderFallback(ServerWebExchange exchange) {
        return writeResponse(exchange, GatewayErrorCode.SERVICE_UNAVAILABLE);
    }

    @RequestMapping(value = "/auth", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public Mono<Void> authFallback(ServerWebExchange exchange) {
        return writeResponse(exchange, GatewayErrorCode.SERVICE_UNAVAILABLE);
    }

    @RequestMapping(value = "/payment", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public Mono<Void> paymentFallback(ServerWebExchange exchange) {
        return writeResponse(exchange, GatewayErrorCode.SERVICE_UNAVAILABLE);
    }

    private Mono<Void> writeResponse(ServerWebExchange exchange, GatewayErrorCode errorCode) {
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        GatewayBaseResponse<Object> body = GatewayBaseResponse.error(errorCode);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (Exception e) {
            return Mono.error(e);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
