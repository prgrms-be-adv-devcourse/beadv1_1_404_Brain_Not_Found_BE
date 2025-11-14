package com.example.settlement.scheduler;

import com.example.settlement.batch.launcher.SettlementJobLauncher;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementsJobScheduler {

    private final SettlementJobLauncher settlementJobLauncher;

    @Scheduled(cron = "0 * * * * *")
    public void executeSettlement() throws Exception {
        settlementJobLauncher.run();
    }

}
