package com.ll.payment.settlement.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SettlementBatchJobLogger implements JobExecutionListener {

    private long start;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        start = System.currentTimeMillis();
        log.info("정산 배치 시작 - Job: {}", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long duration = System.currentTimeMillis() - start;

        log.info("정산 배치 종료 - Job: {}, Status: {}, Duration: {}ms",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus(),
                duration
        );
    }

}