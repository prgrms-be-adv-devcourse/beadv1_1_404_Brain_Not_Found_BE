package com.ll.payment.util;

public final class PaymentCodeGenerator {

    private static final String PAYMENT_PREFIX = "PAY-";

    private PaymentCodeGenerator() {
    }

    public static String newPaymentCode() {
        return PAYMENT_PREFIX + System.currentTimeMillis();
    }
}


