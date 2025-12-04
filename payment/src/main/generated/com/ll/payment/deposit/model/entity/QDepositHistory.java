package com.ll.payment.deposit.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QDepositHistory is a Querydsl query type for DepositHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDepositHistory extends EntityPathBase<DepositHistory> {

    private static final long serialVersionUID = -1189426685L;

    public static final QDepositHistory depositHistory = new QDepositHistory("depositHistory");

    public final com.ll.core.model.persistence.QBaseEntity _super = new com.ll.core.model.persistence.QBaseEntity(this);

    public final NumberPath<Long> amount = createNumber("amount", Long.class);

    public final NumberPath<Long> balanceAfter = createNumber("balanceAfter", Long.class);

    public final NumberPath<Long> balanceBefore = createNumber("balanceBefore", Long.class);

    //inherited
    public final StringPath code = _super.code;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> depositId = createNumber("depositId", Long.class);

    public final EnumPath<com.ll.payment.deposit.model.enums.DepositHistoryType> historyType = createEnum("historyType", com.ll.payment.deposit.model.enums.DepositHistoryType.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath referenceCode = createString("referenceCode");

    public final EnumPath<com.ll.payment.deposit.model.enums.TransactionStatus> transactionStatus = createEnum("transactionStatus", com.ll.payment.deposit.model.enums.TransactionStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QDepositHistory(String variable) {
        super(DepositHistory.class, forVariable(variable));
    }

    public QDepositHistory(Path<? extends DepositHistory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDepositHistory(PathMetadata metadata) {
        super(DepositHistory.class, metadata);
    }

}

