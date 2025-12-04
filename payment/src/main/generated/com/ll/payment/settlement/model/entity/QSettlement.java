package com.ll.payment.settlement.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSettlement is a Querydsl query type for Settlement
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSettlement extends EntityPathBase<Settlement> {

    private static final long serialVersionUID = -2032358573L;

    public static final QSettlement settlement = new QSettlement("settlement");

    public final com.ll.core.model.persistence.QBaseEntity _super = new com.ll.core.model.persistence.QBaseEntity(this);

    public final StringPath buyerCode = createString("buyerCode");

    //inherited
    public final StringPath code = _super.code;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> errorDate = createDateTime("errorDate", java.time.LocalDateTime.class);

    public final StringPath errorMessage = createString("errorMessage");

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath orderItemCode = createString("orderItemCode");

    public final StringPath referenceCode = createString("referenceCode");

    public final StringPath sellerCode = createString("sellerCode");

    public final NumberPath<Long> settlementBalance = createNumber("settlementBalance", Long.class);

    public final NumberPath<Long> settlementCommission = createNumber("settlementCommission", Long.class);

    public final DateTimePath<java.time.LocalDateTime> settlementDate = createDateTime("settlementDate", java.time.LocalDateTime.class);

    public final NumberPath<java.math.BigDecimal> settlementRate = createNumber("settlementRate", java.math.BigDecimal.class);

    public final EnumPath<com.ll.payment.settlement.model.vo.SettlementStatus> settlementStatus = createEnum("settlementStatus", com.ll.payment.settlement.model.vo.SettlementStatus.class);

    public final NumberPath<Long> totalAmount = createNumber("totalAmount", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QSettlement(String variable) {
        super(Settlement.class, forVariable(variable));
    }

    public QSettlement(Path<? extends Settlement> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSettlement(PathMetadata metadata) {
        super(Settlement.class, metadata);
    }

}

