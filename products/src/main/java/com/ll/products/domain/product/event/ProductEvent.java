package com.ll.products.domain.product.event;

import com.ll.products.domain.product.model.entity.Product;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProductEvent extends ApplicationEvent {

    private final Product product;
    private final EventType eventType;

    public ProductEvent(Object source, Product product, EventType eventType) {
        super(source);
        this.product = product;
        this.eventType = eventType;
    }

    public static ProductEvent created(Object source, Product product) {
        return new ProductEvent(source, product, EventType.CREATED);
    }

    public static ProductEvent updated(Object source, Product product) {
        return new ProductEvent(source, product, EventType.UPDATED);
    }

    public static ProductEvent deleted(Object source, Product product) {
        return new ProductEvent(source, product, EventType.DELETED);
    }

    public enum EventType {
        CREATED,
        UPDATED,
        DELETED
    }
}