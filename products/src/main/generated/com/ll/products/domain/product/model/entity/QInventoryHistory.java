package com.ll.products.domain.product.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QInventoryHistory is a Querydsl query type for InventoryHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInventoryHistory extends EntityPathBase<InventoryHistory> {

    private static final long serialVersionUID = -1592915362L;

    public static final QInventoryHistory inventoryHistory = new QInventoryHistory("inventoryHistory");

    public final com.ll.core.model.persistence.QBaseEntity _super = new com.ll.core.model.persistence.QBaseEntity(this);

    public final NumberPath<Integer> afterQuantity = createNumber("afterQuantity", Integer.class);

    public final NumberPath<Integer> beforeQuantity = createNumber("beforeQuantity", Integer.class);

    //inherited
    public final StringPath code = _super.code;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath errorMessage = createString("errorMessage");

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath orderCode = createString("orderCode");

    public final StringPath orderStatus = createString("orderStatus");

    public final DateTimePath<java.time.LocalDateTime> processedAt = createDateTime("processedAt", java.time.LocalDateTime.class);

    public final StringPath productCode = createString("productCode");

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public final StringPath referenceCode = createString("referenceCode");

    public final EnumPath<InventoryHistoryStatus> status = createEnum("status", InventoryHistoryStatus.class);

    public final EnumPath<com.ll.core.model.vo.kafka.enums.InventoryEventType> transactionType = createEnum("transactionType", com.ll.core.model.vo.kafka.enums.InventoryEventType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QInventoryHistory(String variable) {
        super(InventoryHistory.class, forVariable(variable));
    }

    public QInventoryHistory(Path<? extends InventoryHistory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QInventoryHistory(PathMetadata metadata) {
        super(InventoryHistory.class, metadata);
    }

}

