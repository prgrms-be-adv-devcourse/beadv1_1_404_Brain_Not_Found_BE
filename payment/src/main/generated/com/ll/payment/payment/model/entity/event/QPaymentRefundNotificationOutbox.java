package com.ll.payment.payment.model.entity.event;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPaymentRefundNotificationOutbox is a Querydsl query type for PaymentRefundNotificationOutbox
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPaymentRefundNotificationOutbox extends EntityPathBase<PaymentRefundNotificationOutbox> {

    private static final long serialVersionUID = 1826253309L;

    public static final QPaymentRefundNotificationOutbox paymentRefundNotificationOutbox = new QPaymentRefundNotificationOutbox("paymentRefundNotificationOutbox");

    public final com.ll.core.model.persistence.QBaseEntity _super = new com.ll.core.model.persistence.QBaseEntity(this);

    //inherited
    public final StringPath code = _super.code;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath lastErrorMessage = createString("lastErrorMessage");

    public final StringPath notificationPayload = createString("notificationPayload");

    public final DateTimePath<java.time.LocalDateTime> publishedAt = createDateTime("publishedAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> retryCount = createNumber("retryCount", Integer.class);

    public final EnumPath<com.ll.payment.payment.model.enums.PaymentOutboxStatus> status = createEnum("status", com.ll.payment.payment.model.enums.PaymentOutboxStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QPaymentRefundNotificationOutbox(String variable) {
        super(PaymentRefundNotificationOutbox.class, forVariable(variable));
    }

    public QPaymentRefundNotificationOutbox(Path<? extends PaymentRefundNotificationOutbox> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPaymentRefundNotificationOutbox(PathMetadata metadata) {
        super(PaymentRefundNotificationOutbox.class, metadata);
    }

}

