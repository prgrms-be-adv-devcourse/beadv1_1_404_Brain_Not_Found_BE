package com.ll.payment.global.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

@Configuration
public class BatchSchemaInitializer {

    private final DataSource dataSource;

    public BatchSchemaInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void init() {
        if (isBatchSchemaInitialized()) {
            return;
        }

        runBatchSchema();
    }

    private boolean isBatchSchemaInitialized() {
        try (Connection conn = dataSource.getConnection()) {
            try (ResultSet rs = conn.getMetaData().getTables(null, null, "BATCH_JOB_INSTANCE", null)) {
                return rs.next(); // 테이블이 존재하면 true
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to check batch schema", e);
        }
    }

    private void runBatchSchema() {
        String script;

        try (Connection conn = dataSource.getConnection()) {
            String productName = conn.getMetaData().getDatabaseProductName();

            if (productName.contains("H2")) {
                script = "org/springframework/batch/core/schema-h2.sql";
            } else if (productName.contains("MySQL") || productName.contains("MariaDB")) {
                script = "org/springframework/batch/core/schema-mysql.sql";
            } else {
                throw new IllegalStateException("Unsupported database: " + productName);
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to detect database type", e);
        }

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource(script));
        DatabasePopulatorUtils.execute(populator, dataSource);
    }
}
