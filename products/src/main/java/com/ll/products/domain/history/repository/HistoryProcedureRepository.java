package com.ll.products.domain.history.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HistoryProcedureRepository {

    private final EntityManager em;

    public void cleanViewHistory() {
        em.createNativeQuery("CALL clean_view_history()").executeUpdate();
    }

    public void cleanSearchHistory() {
        em.createNativeQuery("CALL clean_search_history()").executeUpdate();
    }
}
