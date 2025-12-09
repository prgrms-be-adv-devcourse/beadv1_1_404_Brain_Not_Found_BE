package com.ll.payment.settlement.batch.config;

import com.ll.payment.settlement.model.entity.Settlement;
import org.springframework.stereotype.Component;

@Component
public class SettlementErrorCapture {
    private final ThreadLocal<Settlement> lastItem = new ThreadLocal<>();
    public void set(Settlement settlement) {
        lastItem.set(settlement);
    }
    public Settlement get() {
        return lastItem.get();
    }
}
