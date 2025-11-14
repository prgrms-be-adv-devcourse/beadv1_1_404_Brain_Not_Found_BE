package com.example.settlement.batch.reader;

import com.example.settlement.model.entity.Settlement;
import com.example.settlement.model.vo.SettlementStatus;
import com.example.settlement.util.SettlementTimeUtils;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SettlementReader  {

    @StepScope
    @Bean("pagingSettlementReader")
    public JpaPagingItemReader<Settlement> PagingSettlementReader(
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
            order by s.id asc
        """);

        reader.setPageSize(batchSize);

        Map<String, Object> params = new HashMap<>();
        params.put("settlementStatus", SettlementStatus.CREATED);
        params.put("startDate", SettlementTimeUtils.getStartDay(dateStr));
        params.put("endDate", SettlementTimeUtils.getEndDay(dateStr));

        reader.setName("pagingSettlementReader");
        reader.setParameterValues(params);

        return reader;
    }

}
