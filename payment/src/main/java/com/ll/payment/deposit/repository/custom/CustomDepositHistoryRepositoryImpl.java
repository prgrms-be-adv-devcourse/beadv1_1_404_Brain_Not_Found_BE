package com.ll.payment.deposit.repository.custom;

import com.ll.core.util.QueryDslSortUtil;
import com.ll.payment.deposit.model.entity.Deposit;
import com.ll.payment.deposit.model.entity.DepositHistory;
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
public class CustomDepositHistoryRepositoryImpl implements CustomDepositHistoryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<DepositHistory> findAllByDepositAndCreatedAtBetween(Deposit deposit, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
        // QDepositHistory 클래스가 주입이 안됨
//        QDepositHistory qHistory = QDepositHistory.depositHistory;
//
//        List<DepositHistory> content = queryFactory.selectFrom(qHistory)
//                .where(qHistory.depositId.eq(deposit.getId()), qHistory.createdAt.goe(fromDate), qHistory.createdAt.lt(toDate))
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize())
//                .orderBy(QueryDslSortUtil.getOrderSpecifiers(pageable, qHistory))
//                .fetch();
//
//        Long total = Optional.ofNullable(
//                queryFactory
//                        .select(qHistory.count())
//                        .from(qHistory)
//                        .where(qHistory.depositId.eq(deposit.getId()), qHistory.createdAt.goe(fromDate), qHistory.createdAt.lt(toDate))
//                        .fetchOne()
//        ).orElse(0L);
//
//        return new PageImpl<>(content, pageable, total);
        return null;
    }
}
