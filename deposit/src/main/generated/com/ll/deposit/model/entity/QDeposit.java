package com.ll.deposit.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QDeposit is a Querydsl query type for Deposit
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDeposit extends EntityPathBase<Deposit> {

    private static final long serialVersionUID = -1809976471L;

    public static final QDeposit deposit = new QDeposit("deposit");

    public final com.ll.core.model.persistence.QBaseEntity _super = new com.ll.core.model.persistence.QBaseEntity(this);

    public final NumberPath<Long> balance = createNumber("balance", Long.class);

    //inherited
    public final StringPath code = _super.code;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<com.ll.deposit.model.enums.DepositStatus> depositStatus = createEnum("depositStatus", com.ll.deposit.model.enums.DepositStatus.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath userCode = createString("userCode");

    public QDeposit(String variable) {
        super(Deposit.class, forVariable(variable));
    }

    public QDeposit(Path<? extends Deposit> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDeposit(PathMetadata metadata) {
        super(Deposit.class, metadata);
    }

}

