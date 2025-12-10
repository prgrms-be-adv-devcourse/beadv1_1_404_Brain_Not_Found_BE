package com.ll.order.domain.mock.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Mock Server Configuration
 * application.yml에서 mock.enabled=true로 설정하면 활성화됩니다.
 */
@Configuration
@ConditionalOnProperty(name = "mock.enabled", havingValue = "true", matchIfMissing = false)
public class MockServerConfig {
    // Mock 서버 활성화를 위한 설정 클래스
    // 실제 구현은 각 Mock Controller에서 처리됩니다.
}

