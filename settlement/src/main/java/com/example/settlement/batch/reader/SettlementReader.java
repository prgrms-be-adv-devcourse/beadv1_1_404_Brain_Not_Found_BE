package com.example.settlement.batch.reader;

import com.example.settlement.model.entity.Settlement;
import com.example.settlement.model.vo.SettlementStatus;
import com.example.settlement.util.SettlementTimeUtils;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@StepScope
@RequiredArgsConstructor
@Component("pagingSettlementReader")
public class SettlementReader extends JpaPagingItemReader<Settlement> {

    public SettlementReader(
            @Value("#{jobParameters['dateStr']}") String dateStr,
            @Value("${custom.batch.chunk.size}") Integer batchSize,
            EntityManagerFactory entityManagerFactory
    ) {

        setEntityManagerFactory(entityManagerFactory);

        setQueryString("""
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

        setPageSize(batchSize);

        Map<String, Object> params = new HashMap<>();
        params.put("settlementStatus", SettlementStatus.CREATED);
        params.put("startDate", SettlementTimeUtils.getStartDay(dateStr));
        params.put("endDate", SettlementTimeUtils.getEndDay(dateStr));

        setName("pagingSettlementReader");
        setParameterValues(params);

    }

}
