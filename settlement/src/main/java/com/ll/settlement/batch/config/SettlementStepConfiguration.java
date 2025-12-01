package com.ll.settlement.batch.config;

import com.ll.core.model.exception.BaseException;
import com.ll.settlement.batch.listener.SettlementBatchStepLogger;
import com.ll.settlement.batch.proccessor.SettlementProcessor;
import com.ll.settlement.model.entity.Settlement;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class SettlementStepConfiguration {
    @Value("${custom.batch.chunk.size:500}")
    private Integer CHUNK_SIZE;
    @Value("${custom.batch.skip.limit:10}")
    private Integer RETRY_LIMIT;

    @Bean
    @Qualifier("settlementStep")
    public Step SettlementStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("pagingSettlementReader") JpaPagingItemReader<Settlement> settlementReader,
            @Qualifier("settlementProcessor") SettlementProcessor settlementProcessor,
            @Qualifier("settlementWriter") ItemWriter<Settlement> settlementWriter,
            SettlementBatchStepLogger logger
    ) {
        return new StepBuilder("settlementStep", jobRepository)
                .<Settlement, Settlement>chunk(CHUNK_SIZE, transactionManager)
                .reader(settlementReader)
                .processor(settlementProcessor)
                .writer(settlementWriter)
                .listener(logger)
                .faultTolerant()
                .skip(BaseException.class)
                .skipLimit(RETRY_LIMIT)
                .build();
    }

}
