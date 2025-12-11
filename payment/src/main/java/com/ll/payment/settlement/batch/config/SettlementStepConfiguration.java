package com.ll.payment.settlement.batch.config;

import com.ll.core.model.exception.BaseException;
import com.ll.payment.settlement.batch.listener.SettlementBatchStepLogger;
import com.ll.payment.settlement.batch.listener.SettlementSkipListener;
import com.ll.payment.settlement.batch.processor.ValidateDepositProcessor;
import com.ll.payment.settlement.batch.processor.SettlementSuccessProcessor;
import com.ll.payment.settlement.model.entity.Settlement;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SettlementStepConfiguration {
    @Value("${custom.batch.chunk.size:100}")
    private Integer CHUNK_SIZE;
    @Value("${custom.batch.skip.limit:100}")
    private Integer SKIP_LIMIT;

    @Bean
    @Qualifier("settlementStep")
    public Step SettlementStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("pagingSettlementReader") JpaPagingItemReader<Settlement> settlementReader,
            @Qualifier("validateDepositProcessor") ValidateDepositProcessor validateDepositProcessor,
            @Qualifier("settlementSuccessProcessor") SettlementSuccessProcessor settlementSuccessProcessor,
            @Qualifier("settlementWriter") ItemWriter<Settlement> settlementWriter,
            SettlementBatchStepLogger logger,
            SettlementSkipListener skipListener
    ) {
        return new StepBuilder("settlementStep", jobRepository)
                .<Settlement, Settlement>chunk(CHUNK_SIZE, transactionManager)
                .reader(settlementReader)
                .processor(settlementProcessor(validateDepositProcessor, settlementSuccessProcessor))
                .writer(settlementWriter)
                .listener(logger)
                .faultTolerant()
                .skip(BaseException.class)
                .skipLimit(SKIP_LIMIT)
                .listener(skipListener)
                .noRollback(BaseException.class)
                .build();
    }

    @Bean
    public CompositeItemProcessor<Settlement, Settlement> settlementProcessor(
            ValidateDepositProcessor validateDepositProcessor,
            SettlementSuccessProcessor settlementSuccessProcessor
    ) {
        CompositeItemProcessor<Settlement, Settlement> processor = new CompositeItemProcessor<>();

        processor.setDelegates(List.of(
                validateDepositProcessor,
                settlementSuccessProcessor
        ));

        return processor;
    }

}
