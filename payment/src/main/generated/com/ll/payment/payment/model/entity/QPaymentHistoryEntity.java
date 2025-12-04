package com.ll.payment.payment.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPaymentHistoryEntity is a Querydsl query type for PaymentHistoryEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPaymentHistoryEntity extends EntityPathBase<PaymentHistoryEntity> {

    private static final long serialVersionUID = -694830394L;

    public static final QPaymentHistoryEntity paymentHistoryEntity = new QPaymentHistoryEntity("paymentHistoryEntity");

    public final com.ll.core.model.persistence.QBaseEntity _super = new com.ll.core.model.persistence.QBaseEntity(this);

    public final NumberPath<Integer> amount = createNumber("amount", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> approvedAt = createDateTime("approvedAt", java.time.LocalDateTime.class);

    //inherited
    public final StringPath code = _super.code;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath currency = createString("currency");

    public final EnumPath<com.ll.payment.payment.model.enums.PaymentHistoryActionType> eventType = createEnum("eventType", com.ll.payment.payment.model.enums.PaymentHistoryActionType.class);

    public final StringPath failCode = createString("failCode");

    public final StringPath failMessage = createString("failMessage");

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath metadata = createString("metadata");

    public final NumberPath<Long> paymentId = createNumber("paymentId", Long.class);

    public final StringPath paymentKey = createString("paymentKey");

    public final EnumPath<com.ll.payment.payment.model.enums.PaymentStatus> paymentStatus = createEnum("paymentStatus", com.ll.payment.payment.model.enums.PaymentStatus.class);

    public final StringPath pgName = createString("pgName");

    public final DateTimePath<java.time.LocalDateTime> refundedAt = createDateTime("refundedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> requestedAt = createDateTime("requestedAt", java.time.LocalDateTime.class);

    public final StringPath transactionId = createString("transactionId");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QPaymentHistoryEntity(String variable) {
        super(PaymentHistoryEntity.class, forVariable(variable));
    }

    public QPaymentHistoryEntity(Path<? extends PaymentHistoryEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPaymentHistoryEntity(PathMetadata metadata) {
        super(PaymentHistoryEntity.class, metadata);
    }

}

