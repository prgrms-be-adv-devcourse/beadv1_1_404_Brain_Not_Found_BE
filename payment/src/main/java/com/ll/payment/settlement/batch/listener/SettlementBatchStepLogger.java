package com.ll.payment.settlement.batch.listener;

import com.ll.payment.settlement.batch.config.SettlementErrorCapture;
import com.ll.payment.settlement.model.entity.Settlement;
import com.ll.payment.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementBatchStepLogger implements StepExecutionListener {
    private long start;
    private final SettlementErrorCapture errorCapture;
    private final SettlementRepository settlementRepository;

    @Override
    public void beforeStep(StepExecution se) {
        start = System.nanoTime();
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {

        if ( stepExecution.getStatus() == BatchStatus.FAILED ) {
            Settlement item = errorCapture.get();

            Throwable exception = stepExecution.getFailureExceptions().getFirst();
            Throwable root = getRootCause(exception);

            item.fail("정산 작업 중 Skip 불가능한 에러 발생: " + root.getClass().getSimpleName() + " - " + root.getMessage());
            settlementRepository.save(item);
        }

        long durMs = (System.nanoTime()-start) / 1_000_000;

        log.info("BATCH_METRIC | Step={} | read={} | write={} | commit={} | processSkip={} | skip={} | duration={}ms",
                stepExecution.getStepName(),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getCommitCount(),
                stepExecution.getProcessSkipCount(),
                stepExecution.getSkipCount(),
                durMs);

        if (stepExecution.getSkipCount() > 0) {
            log.warn("SKIP DETECTED in Step {} - skipCount={}", stepExecution.getStepName(), stepExecution.getSkipCount());
        }

        return stepExecution.getExitStatus();

    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        if (cause == null) return throwable;
        return getRootCause(cause);
    }

}
