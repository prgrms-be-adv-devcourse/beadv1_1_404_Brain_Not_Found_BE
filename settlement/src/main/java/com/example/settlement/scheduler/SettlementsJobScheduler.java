package com.example.settlement.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementsJobScheduler {

    private final SettlementsJobLauncher settlementJobLauncher;

    @Scheduled(cron = "0 * * * * *")
    public void executeSettlement() throws Exception {
        settlementJobLauncher.run();
    }

}
