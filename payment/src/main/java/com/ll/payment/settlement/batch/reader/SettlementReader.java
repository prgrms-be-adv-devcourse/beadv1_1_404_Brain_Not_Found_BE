package com.ll.payment.settlement.batch.reader;

import com.ll.payment.settlement.model.vo.SettlementStatus;
import com.ll.payment.settlement.util.SettlementTimeUtils;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SettlementReader {

    @StepScope
    @Bean("pagingSettlementReader")
    public SettlementPagingItemReader pagingSettlementReader(
            @Value("#{jobParameters['dateStr']}") String dateStr,
            @Value("${custom.batch.chunk.size}") Integer chunkSize,
            EntityManagerFactory entityManagerFactory
    ) {
        return new SettlementPagingItemReader(
                entityManagerFactory,
                chunkSize,
                SettlementStatus.CREATED,
                SettlementTimeUtils.getStartDay(dateStr),
                SettlementTimeUtils.getEndDay(dateStr)
        );
    }

}
