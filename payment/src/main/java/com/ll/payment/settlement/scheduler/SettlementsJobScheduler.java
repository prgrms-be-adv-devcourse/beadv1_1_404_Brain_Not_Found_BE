package com.ll.payment.settlement.scheduler;

import com.ll.payment.settlement.batch.launcher.SettlementJobLauncher;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementsJobScheduler {

    private final SettlementJobLauncher settlementJobLauncher;

    // Todo : 실 사용시 cron expression 변경 필요
    @Scheduled(cron = "0 0 2 14 * *")
    public void executeSettlement() throws Exception {
        settlementJobLauncher.run();
    }

}
