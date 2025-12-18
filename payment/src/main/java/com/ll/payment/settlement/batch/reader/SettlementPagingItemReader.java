package com.ll.payment.settlement.batch.reader;

import com.ll.payment.settlement.model.entity.Settlement;
import com.ll.payment.settlement.model.vo.SettlementStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.AbstractPagingItemReader;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SettlementPagingItemReader
        extends AbstractPagingItemReader<Settlement> {

    private final EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    private final SettlementStatus status;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;

    public SettlementPagingItemReader(
            EntityManagerFactory entityManagerFactory,
            int pageSize,
            SettlementStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        this.entityManagerFactory = entityManagerFactory;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;

        setPageSize(pageSize);
        setName("settlementPagingItemReader");
    }

    @Override
    protected void doOpen() {
        this.entityManager = entityManagerFactory.createEntityManager();
    }

    @Override
    protected void doClose() {
        if (entityManager != null) {
            entityManager.close();
        }
    }

    @Override
    protected void doReadPage() {

        if (results == null) {
            results = new ArrayList<>(getPageSize());
        } else {
            results.clear();
        }

        List<Settlement> page = entityManager.createQuery("""
            select s
            from Settlement s
            where s.createdAt between :startDate and :endDate
              and s.settlementStatus = :status
            order by s.id
        """, Settlement.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("status", status)
                .setFirstResult(0)
                .setMaxResults(getPageSize())
                .getResultList();

        for (Settlement settlement : page) {
            entityManager.detach(settlement);
        }

        results.addAll(page);
    }
}