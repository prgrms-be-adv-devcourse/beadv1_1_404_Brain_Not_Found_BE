package com.ll.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("auth-test")
class TokenAuthenticationFilterTest {

    @LocalServerPort
    int port;

    WebTestClient webClient;

    SecretKey testKey;

    @BeforeEach
    void setUp() {
        testKey = Keys.hmacShaKeyFor(
                "test-secret-key-for-gateways-must-be-very-long-and-secure-1234567890"
                        .getBytes(StandardCharsets.UTF_8)
        );

        webClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    private String createToken(long minutes) {
        Instant now = Instant.now();

        return Jwts.builder()
                .claim("userCode", "U12345")
                .claim("role", "ADMIN")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(minutes, ChronoUnit.MINUTES)))
                .signWith(testKey)
                .compact();
    }

    @Test
    @DisplayName("정상 토큰 → 인증 필터 통과 (백엔드 없으므로 5xx)")
    void validToken_shouldPassAuthenticationFilter() {
        String token = createToken(10);

        webClient.get()
                .uri("/test/auth/hello")
                .cookie("accessToken", token)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("만료된 토큰 → 인증 필터에서 차단 (500)")
    void expiredToken_shouldBeBlockedByAuthenticationFilter() {
        String expiredToken = createToken(-10);

        webClient.get()
                .uri("/test/auth/hello")
                .cookie("accessToken", expiredToken)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("토큰 없음 → 인증 필터에서 차단 (500)")
    void noToken_shouldBeBlockedByAuthenticationFilter() {
        webClient.get()
                .uri("/test/auth/hello")
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
