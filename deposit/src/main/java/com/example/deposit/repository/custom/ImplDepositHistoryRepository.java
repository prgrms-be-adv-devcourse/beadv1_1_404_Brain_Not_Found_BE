package com.example.deposit.repository.custom;

import com.example.core.util.QueryDslSortUtil;
import com.example.deposit.model.entity.Deposit;
import com.example.deposit.model.entity.DepositHistory;
import com.example.deposit.model.entity.QDepositHistory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ImplDepositHistoryRepository implements CustomDepositHistoryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<DepositHistory> findAllByDepositAndCreatedAtBetween(Deposit deposit, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
        QDepositHistory qHistory = QDepositHistory.depositHistory;

        List<DepositHistory> content = queryFactory.selectFrom(qHistory)
                .where(qHistory.deposit.eq(deposit), qHistory.createdAt.goe(fromDate), qHistory.createdAt.lt(toDate))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(QueryDslSortUtil.getOrderSpecifiers(pageable, qHistory))
                .fetch();

        Long total = Optional.ofNullable(
                queryFactory
                        .select(qHistory.count())
                        .from(qHistory)
                        .where(qHistory.deposit.eq(deposit), qHistory.createdAt.goe(fromDate), qHistory.createdAt.lt(toDate))
                        .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }
}
