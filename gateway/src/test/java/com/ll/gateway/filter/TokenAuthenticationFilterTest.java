//package com.ll.gateway.filter;
//
//import com.ll.gateway.exception.GatewayErrorCode;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.reactive.server.WebTestClient;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//class TokenAuthenticationFilterTest {
//
//    @Autowired
//    private WebTestClient webTestClient;
//
//    @Autowired
//    private TokenAuthenticationFilter tokenAuthenticationFilter;
//
//    private String validToken;
//    private String invalidToken = "invalid.token.value";
//
//    @BeforeEach
//    void setUp() {
//        // 테스트용 validToken 생성: 실제로는 JWT 라이브러리로 payload/signature 생성
//        validToken = "Bearer valid.jwt.token";
//    }
//
//    @Test
//    void whenNoAuthorizationHeader_thenReturns401() {
//        webTestClient.get()
//                .uri("/api/users")
//                .exchange()
//                .expectStatus().isUnauthorized()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody()
//                .jsonPath("$.status").isEqualTo(401)
//                .jsonPath("$.errorCode").isEqualTo(GatewayErrorCode.UNAUTHORIZED.name());
//    }
//
//    @Test
//    void whenInvalidToken_thenReturns401() {
//        webTestClient.get()
//                .uri("/api/users")
//                .header(HttpHeaders.AUTHORIZATION, invalidToken)
//                .exchange()
//                .expectStatus().isUnauthorized()
//                .expectBody()
//                .jsonPath("$.status").isEqualTo(401)
//                .jsonPath("$.errorCode").isEqualTo(GatewayErrorCode.UNAUTHORIZED.name());
//    }
//
//    @Test
//    void whenValidToken_thenPassesThrough() {
//        // 여기는 실제 JWT 토큰과 claims 검증 로직이 필요
//        // WebTestClient로 호출 시 필터를 거쳐서 정상 200 응답 확인
//        webTestClient.get()
//                .uri("/api/users")
//                .header(HttpHeaders.AUTHORIZATION, validToken)
//                .exchange()
//                .expectStatus().isOk();
//    }
//}
