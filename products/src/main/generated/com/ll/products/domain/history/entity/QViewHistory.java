package com.ll.products.domain.history.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QViewHistory is a Querydsl query type for ViewHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QViewHistory extends EntityPathBase<ViewHistory> {

    private static final long serialVersionUID = -1834386177L;

    public static final QViewHistory viewHistory = new QViewHistory("viewHistory");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath productCode = createString("productCode");

    public final StringPath userCode = createString("userCode");

    public QViewHistory(String variable) {
        super(ViewHistory.class, forVariable(variable));
    }

    public QViewHistory(Path<? extends ViewHistory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QViewHistory(PathMetadata metadata) {
        super(ViewHistory.class, metadata);
    }

}

