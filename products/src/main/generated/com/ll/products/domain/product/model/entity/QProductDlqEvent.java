package com.ll.products.domain.product.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QProductDlqEvent is a Querydsl query type for ProductDlqEvent
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProductDlqEvent extends EntityPathBase<ProductDlqEvent> {

    private static final long serialVersionUID = 1870848442L;

    public static final QProductDlqEvent productDlqEvent = new QProductDlqEvent("productDlqEvent");

    public final com.ll.core.model.persistence.QBaseEntity _super = new com.ll.core.model.persistence.QBaseEntity(this);

    //inherited
    public final StringPath code = _super.code;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath eventPayload = createString("eventPayload");

    public final DateTimePath<java.time.LocalDateTime> failedAt = createDateTime("failedAt", java.time.LocalDateTime.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final EnumPath<DlqStatus> status = createEnum("status", DlqStatus.class);

    public final StringPath topic = createString("topic");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QProductDlqEvent(String variable) {
        super(ProductDlqEvent.class, forVariable(variable));
    }

    public QProductDlqEvent(Path<? extends ProductDlqEvent> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProductDlqEvent(PathMetadata metadata) {
        super(ProductDlqEvent.class, metadata);
    }

}

