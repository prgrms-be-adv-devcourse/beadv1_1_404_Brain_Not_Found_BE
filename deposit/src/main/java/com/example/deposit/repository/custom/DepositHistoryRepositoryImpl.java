package com.example.deposit.repository.custom;

import com.example.core.util.QueryDslSortUtil;
import com.example.deposit.model.entity.Deposit;
import com.example.deposit.model.entity.DepositHistory;
import com.example.deposit.model.entity.QDepositHistory;
import com.querydsl.core.types.dsl.BooleanExpression;
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
public class DepositHistoryRepositoryImpl implements  DepositHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<DepositHistory> findAllByDepositAndCreatedAtBetween(Deposit deposit, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
        QDepositHistory qHistory = QDepositHistory.depositHistory;

        BooleanExpression fromDateEx = fromDate != null ? qHistory.createdAt.goe(fromDate) : null;
        BooleanExpression toDateEx = toDate != null ? qHistory.createdAt.lt(toDate) : null;

        List<DepositHistory> content = queryFactory.selectFrom(qHistory)
                .where(qHistory.deposit.eq(deposit), fromDateEx, toDateEx)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(QueryDslSortUtil.getOrderSpecifiers(pageable, qHistory)) // 기본 정렬 예시
                .fetch();

        Long total = Optional.ofNullable(
                queryFactory
                        .select(qHistory.count())
                        .from(qHistory)
                        .where(qHistory.deposit.eq(deposit), fromDateEx, toDateEx)
                        .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }
}
