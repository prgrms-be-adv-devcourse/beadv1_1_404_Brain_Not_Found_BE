package com.ll.products.domain.history.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProcedureInitializer {

    private final DataSource dataSource;

    @PostConstruct
    public void init() {
        createViewHistoryProcedure();
        createSearchHistoryProcedure();
    }

    private void createViewHistoryProcedure() {
        String dropSql = "DROP PROCEDURE IF EXISTS clean_view_history";

        String createSql = """
            CREATE PROCEDURE clean_view_history()
            BEGIN
                DECLARE cutoff_id BIGINT DEFAULT NULL;

                SELECT id INTO cutoff_id
                FROM view_history
                ORDER BY id DESC
                LIMIT 1 OFFSET 49999;

                IF cutoff_id IS NOT NULL THEN
                    DELETE FROM view_history WHERE id < cutoff_id;
                END IF;
            END
            """;

        executeProcedure("clean_view_history", dropSql, createSql);
    }

    private void createSearchHistoryProcedure() {
        String dropSql = "DROP PROCEDURE IF EXISTS clean_search_history";

        String createSql = """
            CREATE PROCEDURE clean_search_history()
            BEGIN
                DECLARE cutoff_id BIGINT DEFAULT NULL;

                SELECT id INTO cutoff_id
                FROM search_history
                ORDER BY id DESC
                LIMIT 1 OFFSET 49999;

                IF cutoff_id IS NOT NULL THEN
                    DELETE FROM search_history WHERE id < cutoff_id;
                END IF;
            END
            """;

        executeProcedure("clean_search_history", dropSql, createSql);
    }

    private void executeProcedure(String name, String dropSql, String createSql) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(dropSql);
            stmt.execute(createSql);

            log.info("{} 프로시저 자동 생성 완료", name);

        } catch (Exception e) {
            log.warn("{} 프로시저 생성 실패: {}", name, e.getMessage());
        }
    }
}
