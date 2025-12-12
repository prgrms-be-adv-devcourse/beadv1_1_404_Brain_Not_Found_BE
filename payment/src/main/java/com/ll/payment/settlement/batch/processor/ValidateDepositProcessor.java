package com.ll.payment.settlement.batch.processor;

import com.ll.payment.deposit.model.vo.request.DepositTransactionRequest;
import com.ll.payment.deposit.service.DepositHistoryService;
import com.ll.payment.settlement.batch.config.SettlementErrorCapture;
import com.ll.payment.settlement.model.entity.Settlement;
import com.ll.payment.settlement.model.vo.SettlementProcessDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component("validateDepositProcessor")
@RequiredArgsConstructor
public class ValidateDepositProcessor implements ItemProcessor<Settlement, SettlementProcessDTO> {

    private final DepositHistoryService depositHistoryService;
    private final SettlementErrorCapture errorCapture;

    @Override
    public SettlementProcessDTO process(Settlement settlement) {
        errorCapture.set(settlement);
        DepositTransactionRequest request = validationReferenceCode(settlement);
        return new SettlementProcessDTO(settlement, request);
    }

    private DepositTransactionRequest validationReferenceCode(Settlement settlement) {
        DepositTransactionRequest request = DepositTransactionRequest
                .of(settlement.getSettlementBalance(), settlement.getProductName(), settlement.getOrderItemCode());
        depositHistoryService.validateDuplicate(request.referenceCode());
        return request;
    }

}
