package com.ll.gateway.filter;

import com.ll.gateway.exception.GatewayBaseException;
import com.ll.gateway.exception.GatewayErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
@Component
@Slf4j
public class TokenAuthenticationFilter extends AbstractGatewayFilterFactory<TokenAuthenticationFilter.Config> {

    @Value("${custom.jwt.secrets.app-key}")
    private String secretKey;

    private SecretKey key;  // HS512 서명 키

    @PostConstruct  // 빈 초기화 시 키 생성
    public void init() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }


    public static class Config {
    }

    public TokenAuthenticationFilter() {
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
            getUserCodeDto result = getUserCode(claims);

            ServerHttpRequest mutatedRequest = setUserCodeAtHeader(request, result);
            log.info("Header user code added");
            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        };
    }

    private static ServerHttpRequest setUserCodeAtHeader(ServerHttpRequest request, getUserCodeDto result) {
        return request.mutate()
                .header("X-User-Code", result.userCode())
                .header("X-Role", result.role())
                .build();
    }

    private static getUserCodeDto getUserCode(Jws<Claims> claims) {
        String userCode = claims.getPayload().get("userCode", String.class);
        String role = claims.getPayload().get("role", String.class);
        log.info("user code is {}", userCode);
        log.info("role is {}", role);
        return new getUserCodeDto(userCode, role);
    }

    private record getUserCodeDto(String userCode, String role) {
    }

    private static String getTokenFromCookie(ServerHttpRequest request) {
        HttpCookie cookie = request.getCookies().getFirst("accessToken");
        return (cookie != null) ? cookie.getValue() : null;
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
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }
}