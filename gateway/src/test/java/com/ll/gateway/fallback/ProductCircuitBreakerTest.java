package com.ll.gateway.fallback;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("cb-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductCircuitBreakerTest {

    static WireMockServer wireMockServer;

    @LocalServerPort
    int port;

    WebTestClient webClient;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();

        registry.add(
                "wiremock.server.port",
                () -> wireMockServer.port()
        );
    }

    @BeforeAll
    void setUpClient() {
        configureFor("localhost", wireMockServer.port());

        webClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @AfterAll
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    @DisplayName("productServiceCB OPEN 시 fallback 컨트롤러 응답 검증")
    void circuitBreaker_shouldFallback() {

        stubFor(get(urlMatching("/test/products/.*"))
                .willReturn(aResponse()
                        .withFixedDelay(5000)));

        // 1차
        webClient.get()
                .uri("/test/products/1")
                .exchange()
                .expectStatus().is5xxServerError();

        // 2차 → 서킷 OPEN
        webClient.get()
                .uri("/test/products/1")
                .exchange()
                .expectStatus().is5xxServerError();

        // 3차 → fallback
        webClient.get()
                .uri("/test/products/1")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("허용되지 않은 접근입니다.");
    }

}
