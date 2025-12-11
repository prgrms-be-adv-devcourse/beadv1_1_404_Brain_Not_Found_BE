package com.ll.payment.settlement.batch.listener;

import com.ll.payment.settlement.model.entity.Settlement;
import com.ll.payment.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementSkipListener implements SkipListener<Settlement, Settlement> {

    private final SettlementRepository settlementRepository;

    @Override
    public void onSkipInProcess(Settlement item, Throwable t) {
        item.fail("정산 작업 중 에러 발생: " + t.getMessage());
        settlementRepository.save(item);
    }

}
