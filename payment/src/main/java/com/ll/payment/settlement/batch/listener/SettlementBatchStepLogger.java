package com.ll.payment.settlement.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SettlementBatchStepLogger implements StepExecutionListener {
    private long start;

    @Override
    public void beforeStep(StepExecution se) {
        start = System.nanoTime();
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {

        long durMs = (System.nanoTime()-start) / 1_000_000;

        log.info("BATCH_METRIC | Step={} | read={} | write={} | commit={} | skip={} | duration={}ms",
                stepExecution.getStepName(),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getCommitCount(),
                stepExecution.getSkipCount(),
                durMs);

        if (stepExecution.getSkipCount() > 0) {
            log.warn("SKIP DETECTED in Step {} - skipCount={}", stepExecution.getStepName(), stepExecution.getSkipCount());
        }

        return stepExecution.getExitStatus();

    }
}
