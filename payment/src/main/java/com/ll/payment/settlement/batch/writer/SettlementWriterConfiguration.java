package com.ll.payment.settlement.batch.writer;

import com.ll.payment.settlement.model.entity.Settlement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.sql.DataSource;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementWriterConfiguration {

    private final DataSource dataSource;

    @Bean("settlementJdbcWriter")
    @StepScope
    public JdbcBatchItemWriter<Settlement> settlementJdbcWriter() {
        JdbcBatchItemWriter<Settlement> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(dataSource);
        writer.setSql("""
            UPDATE settlements
            SET
                settlement_status = :settlementStatus,
                settlement_date = :settlementDate
            WHERE
                id = :id
                AND code = :code
        """);

        writer.setAssertUpdates(true);
        writer.setItemSqlParameterSourceProvider(
                item -> new MapSqlParameterSource()
                        .addValue("settlementStatus", item.getSettlementStatus().name())
                        .addValue("settlementDate", item.getSettlementDate())
                        .addValue("id", item.getId())
                        .addValue("code", item.getCode())
        );

        writer.afterPropertiesSet();
        return writer;
    }
}