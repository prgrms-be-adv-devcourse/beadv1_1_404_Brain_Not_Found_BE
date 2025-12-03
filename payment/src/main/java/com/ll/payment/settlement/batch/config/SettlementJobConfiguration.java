package com.ll.payment.settlement.batch.config;

import com.ll.payment.settlement.batch.listener.SettlementBatchJobLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SettlementJobConfiguration {

    @Bean
    @Qualifier("settlementsJob")
    public Job settlementsJob(
            JobRepository jobRepository,
            @Qualifier("settlementStep") Step settlementStep,
            SettlementBatchJobLogger logger
    ) {
        return new JobBuilder("settlementsJob", jobRepository)
                .listener(logger)
                .start(settlementStep)
                .build();
    }

}
