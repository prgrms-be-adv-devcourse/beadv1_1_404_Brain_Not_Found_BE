package com.ll.products.domain.history.service;

import com.ll.products.domain.history.repository.HistoryProcedureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoryCleanupScheduler {

    private final HistoryProcedureRepository historyProcedureRepository;

    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시
    public void cleanViewHistory() {
        historyProcedureRepository.cleanViewHistory();
        log.info("[스케줄러] ViewHistory 정리 완료");
    }

    @Scheduled(cron = "0 10 3 * * *") // 3시 10분
    public void cleanSearchHistory() {
        historyProcedureRepository.cleanSearchHistory();
        log.info("[스케줄러] SearchHistory 정리 완료");
    }
}

