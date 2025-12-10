package com.ll.products.domain.history.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class HistoryProcedureRepository {

    private final EntityManager em;

    @Transactional
    public void cleanViewHistory() {
        em.createNativeQuery("CALL clean_view_history()").executeUpdate();
    }

    @Transactional
    public void cleanSearchHistory() {
        em.createNativeQuery("CALL clean_search_history()").executeUpdate();
    }
}
