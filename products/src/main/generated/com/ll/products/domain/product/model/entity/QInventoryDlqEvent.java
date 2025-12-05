package com.ll.products.domain.product.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QInventoryDlqEvent is a Querydsl query type for InventoryDlqEvent
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInventoryDlqEvent extends EntityPathBase<InventoryDlqEvent> {

    private static final long serialVersionUID = 2045000359L;

    public static final QInventoryDlqEvent inventoryDlqEvent = new QInventoryDlqEvent("inventoryDlqEvent");

    public final com.ll.core.model.persistence.QBaseEntity _super = new com.ll.core.model.persistence.QBaseEntity(this);

    //inherited
    public final StringPath code = _super.code;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath errorMessage = createString("errorMessage");

    public final EnumPath<com.ll.core.model.vo.kafka.enums.InventoryEventType> eventType = createEnum("eventType", com.ll.core.model.vo.kafka.enums.InventoryEventType.class);

    public final DateTimePath<java.time.LocalDateTime> failedAt = createDateTime("failedAt", java.time.LocalDateTime.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final DateTimePath<java.time.LocalDateTime> processedAt = createDateTime("processedAt", java.time.LocalDateTime.class);

    public final StringPath processedBy = createString("processedBy");

    public final StringPath productCode = createString("productCode");

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public final StringPath referenceCode = createString("referenceCode");

    public final NumberPath<Integer> retryCount = createNumber("retryCount", Integer.class);

    public final EnumPath<DlqStatus> status = createEnum("status", DlqStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QInventoryDlqEvent(String variable) {
        super(InventoryDlqEvent.class, forVariable(variable));
    }

    public QInventoryDlqEvent(Path<? extends InventoryDlqEvent> path) {
        super(path.getType(), path.getMetadata());
    }

    public QInventoryDlqEvent(PathMetadata metadata) {
        super(InventoryDlqEvent.class, metadata);
    }

}

