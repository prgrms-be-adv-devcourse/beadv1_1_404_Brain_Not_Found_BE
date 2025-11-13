package com.example.settlement.batch.reader;

import com.example.settlement.model.entity.Settlement;
import com.example.settlement.model.vo.SettlementStatus;
import com.example.settlement.util.SettlementTimeUtils;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SettlementsReaderConfiguration {

    @Bean
    @StepScope
    public JpaPagingItemReader<Settlement> pagingOrderItemReader(
            @Value("#{jobParameters['dateStr']}") String dateStr,
            @Value("${custom.batch.chunk.size}") Integer batchSize,
            EntityManagerFactory entityManagerFactory
    ) {
        JpaPagingItemReader<Settlement> reader = new JpaPagingItemReader<>();

        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("""
            select
                s
            from
                Settlement s
            where
                s.createdAt between :startDate and :endDate
            and
                s.settlementStatus = :settlementStatus
            and
                s.settlementDate is null
        """);

        reader.setPageSize(batchSize);

        Map<String, Object> params = new HashMap<>();
        params.put("settlementStatus", SettlementStatus.CREATED);
        params.put("startDate", SettlementTimeUtils.getStartDay(dateStr));
        params.put("endDate", SettlementTimeUtils.getEndDay(dateStr));

        reader.setParameterValues(params);
        reader.setName("pagingSettlementReader");

        return reader;
    }

    @Bean
    @StepScope
    public JpaCursorItemReader<Settlement> cursorOrderItemReader(
            @Value("#{jobParameters['dateStr']}") String dateStr,
            EntityManagerFactory entityManagerFactory
    ) {
        JpaCursorItemReader<Settlement> reader = new JpaCursorItemReader<>();

        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("""
            select
                s
            from
                Settlement s
            where
                s.createdAt between :startDate and :endDate
            and
                s.settlementStatus = :settlementStatus
            and
                s.settlementDate is null
        """);

        Map<String, Object> params = new HashMap<>();
        params.put("startDate", SettlementTimeUtils.getStartDay(dateStr));
        params.put("endDate", SettlementTimeUtils.getEndDay(dateStr));

        reader.setParameterValues(params);
        reader.setName("cursorSettlementReader");

        return reader;
    }

}
