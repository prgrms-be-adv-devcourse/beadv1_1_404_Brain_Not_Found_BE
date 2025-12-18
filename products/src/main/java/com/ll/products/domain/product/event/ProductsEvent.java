package com.ll.products.domain.product.event;

import com.ll.products.domain.product.model.entity.Product;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProductsEvent extends ApplicationEvent {

    private final Product product;
    private final EventType eventType;

    public ProductsEvent(Object source, Product product, EventType eventType) {
        super(source);
        this.product = product;
        this.eventType = eventType;
    }

    public static ProductsEvent created(Object source, Product product) {
        return new ProductsEvent(source, product, EventType.CREATED);
    }

    public static ProductsEvent updated(Object source, Product product) {
        return new ProductsEvent(source, product, EventType.UPDATED);
    }

    public static ProductsEvent deleted(Object source, Product product) {
        return new ProductsEvent(source, product, EventType.DELETED);
    }

    public static ProductsEvent updatedStatus(Object source, Product product) {
        return new ProductsEvent(source, product, EventType.UPDATED_STATUS);
    }

    public enum EventType {
        CREATED,
        UPDATED,
        DELETED,
        UPDATED_STATUS
    }
}