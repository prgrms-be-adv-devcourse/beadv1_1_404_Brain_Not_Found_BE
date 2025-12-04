package com.ll.payment.payment.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPayment is a Querydsl query type for Payment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPayment extends EntityPathBase<Payment> {

    private static final long serialVersionUID = -885524911L;

    public static final QPayment payment = new QPayment("payment");

    public final com.ll.core.model.persistence.QBaseEntity _super = new com.ll.core.model.persistence.QBaseEntity(this);

    public final NumberPath<Long> buyerId = createNumber("buyerId", Long.class);

    //inherited
    public final StringPath code = _super.code;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> depositHistoryId = createNumber("depositHistoryId", Long.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final NumberPath<Long> orderId = createNumber("orderId", Long.class);

    public final NumberPath<Integer> paidAmount = createNumber("paidAmount", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> paidAt = createDateTime("paidAt", java.time.LocalDateTime.class);

    public final EnumPath<com.ll.payment.payment.model.enums.PaidType> paidType = createEnum("paidType", com.ll.payment.payment.model.enums.PaidType.class);

    public final StringPath paymentKey = createString("paymentKey");

    public final EnumPath<com.ll.payment.payment.model.enums.PaymentStatus> paymentStatus = createEnum("paymentStatus", com.ll.payment.payment.model.enums.PaymentStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QPayment(String variable) {
        super(Payment.class, forVariable(variable));
    }

    public QPayment(Path<? extends Payment> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPayment(PathMetadata metadata) {
        super(Payment.class, metadata);
    }

}

