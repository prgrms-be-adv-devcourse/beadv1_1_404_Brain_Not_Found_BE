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
public class TokenAuthenticationFilter extends AbstractGatewayFilterFactory<TokenAuthenticationFilter.Config> {

    @Value("${custom.jwt.secrets.app-key}")
    private String secretKey;

    private final ObjectMapper om = new ObjectMapper();

    public static class Config {
    }

    public TokenAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                log.error("Token authentication header not found!");
                return Mono.error(new GatewayBaseException(GatewayErrorCode.UNAUTHORIZED));

            }
            //토큰 값이 정해진 규칙이 아닐 때 때
            Optional<String> tokenOptional = resolveToken(request);
            log.error("Token value is incorrect!");
            if (tokenOptional.isEmpty()) {
                return Mono.error(new GatewayBaseException(GatewayErrorCode.UNAUTHORIZED));
            }
            String token = tokenOptional.get();
            //올바른 토큰 값 형식이 아닐 때
            if (!isValidToken(token)) {
                log.error("Token value is invalid!");
                return Mono.error(new GatewayBaseException(GatewayErrorCode.UNAUTHORIZED));
            }
            Jws<Claims> claims = getClaims(token);
            String userCode = claims.getPayload().get("userCode", String.class);
            String role = claims.getPayload().get("role", String.class);
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Code", userCode)
                    .header("X-Role", role)
                    .build();
            log.info("Header user code added");
            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        };
    }

    private byte[] writeResponseBody(GatewayBaseResponse<Object> response) {
        try {
            return om.writeValueAsBytes(response);
        } catch (JsonProcessingException e) {
            log.error("Serialization error");
            throw new RuntimeException(e);
        }
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


