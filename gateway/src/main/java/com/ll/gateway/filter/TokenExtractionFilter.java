package com.ll.gateway.filter;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.gateway.exception.GatewayBaseException;
import com.ll.gateway.exception.GatewayErrorCode;
import com.ll.gateway.resopnse.GatewayBaseResponse;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@Slf4j
public class TokenExtractionFilter extends AbstractGatewayFilterFactory<TokenExtractionFilter.Config> {

    @Value("${custom.jwt.secrets.app-key}")
    private String secretKey;

    private final ObjectMapper om = new ObjectMapper();

    public static class Config {
    }

    public TokenExtractionFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();

            Optional<String> tokenOptional = resolveToken(request);

            if (tokenOptional.isEmpty()) {
                log.info("No token found. Skip token extraction.");
                return chain.filter(exchange);
            }

            String token = tokenOptional.get();

            if (!isValidToken(token)) {
                log.info("Invalid or expired token. Skipping token extraction.");
                return chain.filter(exchange);
            }

            Jws<Claims> claims = getClaims(token);
            String userCode = claims.getPayload().get("userCode", String.class);
            String role = claims.getPayload().get("role", String.class);

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Code", userCode)
                    .header("X-Role", role)
                    .build();

            log.info("Injected userCode={}, role={} into headers", userCode, role);

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        };
    }


    private Optional<String> resolveToken(ServerHttpRequest request) {

        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return Optional.of(bearerToken.substring(7));
        }
        return Optional.empty();
    }

    private boolean isValidToken(String token) {

        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.info("Access Token expired msg : {}", e.getMessage());
        } catch (JwtException e) {
            log.info("Invalid JWT Token was detected msg : {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.info("JWT claims String is empty msg : {}", e.getMessage());
        } catch (Exception e) {
            log.error("an error raised from validating token msg : {}", e.getMessage());
        }
        return false;
    }

    private Jws<Claims> getClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseSignedClaims(token);
    }

}



