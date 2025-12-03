package com.ll.payment.settlement.batch.launcher;

import com.ll.payment.settlement.util.SettlementTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementJobLauncher {

    private final JobLauncher jobLauncher;
    private final Job settlementsJob;

    public void run() throws Exception {

        log.info("Starting settlement job");

        String previousMonth = SettlementTimeUtils.getPreviousMonthStr();

        log.info("Running settlement job for previous month: {}", previousMonth);

        JobParameters parameters = new JobParametersBuilder()
                .addString("dateStr", previousMonth)
                .addString("run.id", UUID.randomUUID().toString())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(settlementsJob, parameters);

        log.info("Job finished with status: {}", execution.getStatus());

    }

}
