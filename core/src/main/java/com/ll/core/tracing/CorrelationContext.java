package com.ll.core.tracing;

public class CorrelationContext {

    private static final ThreadLocal<String> context = new ThreadLocal<>();

    public static void set(String correlationId) {
        context.set(correlationId);
    }

    public static String get() {
        return context.get();
    }

    public static void clear() {
        context.remove();
    }
}
