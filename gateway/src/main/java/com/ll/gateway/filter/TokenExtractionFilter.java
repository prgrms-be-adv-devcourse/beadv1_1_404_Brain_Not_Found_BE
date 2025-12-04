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
import org.springframework.http.HttpCookie;
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

            String token = getTokenFromCookie(request);

            //올바른 토큰 값 형식이 아닐 때
            if (!isValidToken(token)) {
                log.error("Token value is invalid!");
                return Mono.error(new GatewayBaseException(GatewayErrorCode.UNAUTHORIZED));
            }

            Jws<Claims> claims = getClaims(token);
            TokenExtractionFilter.getUserCodeDto result = getUserCode(claims);

            ServerHttpRequest mutatedRequest = setUserCodeAtHeader(request, result);
            log.info("Header user code added");
            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        };
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


    private static String getTokenFromCookie(ServerHttpRequest request) {
        HttpCookie cookie = request.getCookies().getFirst("accessToken");
        return (cookie != null) ? cookie.getValue() : null;
    }

    private Jws<Claims> getClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseSignedClaims(token);
    }

    private static ServerHttpRequest setUserCodeAtHeader(ServerHttpRequest request, TokenExtractionFilter.getUserCodeDto result) {
        return request.mutate()
                .header("X-User-Code", result.userCode())
                .header("X-Role", result.role())
                .build();
    }

    private static TokenExtractionFilter.getUserCodeDto getUserCode(Jws<Claims> claims) {
        String userCode = claims.getPayload().get("userCode", String.class);
        String role = claims.getPayload().get("role", String.class);
        log.info("user code is {}", userCode);
        log.info("role is {}", role);
        return new TokenExtractionFilter.getUserCodeDto(userCode, role);
    }

    private record getUserCodeDto(String userCode, String role) {
    }
}



