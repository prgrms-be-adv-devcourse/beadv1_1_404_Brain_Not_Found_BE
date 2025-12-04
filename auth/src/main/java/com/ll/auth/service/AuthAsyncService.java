    package com.ll.auth.service;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.scheduling.annotation.Async;
    import org.springframework.stereotype.Service;

    @Service
    @RequiredArgsConstructor
    @Slf4j
    public class AuthAsyncService {


        private final AuthSyncService authService;

        @Async
        public void asyncDelete(String refreshToken) {
            authService.delete(refreshToken);
            log.info("Async DB delete 완료: refreshToken={}", refreshToken);
        }

        @Async
        public void asyncUpsert(String userCode, String deviceCode, String refreshToken) {
            authService.upsert(userCode, deviceCode, refreshToken);
            log.info("Async DB upsert 완료");
        }
    }
