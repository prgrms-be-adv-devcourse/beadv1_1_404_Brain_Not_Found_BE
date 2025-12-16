package com.ll.payment.settlement.batch.processor;

import com.ll.payment.deposit.service.DepositService;
import com.ll.payment.settlement.model.entity.Settlement;
import com.ll.payment.settlement.model.vo.SettlementProcessDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration("settlementSuccessProcessor")
@RequiredArgsConstructor
public class SettlementSuccessProcessor implements ItemProcessor<SettlementProcessDTO, Settlement> {

    private final DepositService depositService;

    @Override
    public Settlement process(SettlementProcessDTO dto) {
        dto.settlement().done();
        depositService.settlementDeposit(dto.settlement().getSellerCode(), dto.request());
        return dto.settlement();
    }

}
