package com.ll.payment.deposit;

import com.fasterxml.uuid.Generators;
import com.ll.payment.deposit.model.entity.Deposit;
import com.ll.payment.deposit.model.entity.DepositHistory;
import com.ll.payment.deposit.model.enums.DepositHistoryType;
import com.ll.payment.deposit.model.exception.*;
import com.ll.payment.deposit.model.vo.request.DepositTransactionRequest;
import com.ll.payment.deposit.repository.DepositHistoryRepository;
import com.ll.payment.deposit.service.DepositHistoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"NonAsciiCharacters", "FieldCanBeLocal"})
@ExtendWith(MockitoExtension.class)
@DisplayName("DepositHistoryService 단위 테스트")
public class DepositHistoryServiceUnitTest {

    @Mock
    private DepositHistoryRepository depositHistoryRepository;

    @InjectMocks
    private DepositHistoryServiceImpl depositHistoryService;

    private final String USER_CODE = "User_" + Generators.timeBasedGenerator().generate().toString();
    private final String REF_CODE = Generators.timeBasedGenerator().generate().toString();
    private final Long AMOUNT = 5000L;

    private final Long AMOUNT_SETUP = 1000L;

    private Deposit deposit;

    @BeforeEach
    void setUp() {
        deposit = Deposit.createInitialDeposit(USER_CODE);
        deposit.charge(AMOUNT_SETUP, REF_CODE);
    }

    /* ================
        Helper Methods
       ================ */

    @Test
    void 중복이_없으면_정상종료한다() {
        // given
        when(depositHistoryRepository.existsByReferenceCode(REF_CODE)).thenReturn(false);

        // when & then
        assertDoesNotThrow(() -> depositHistoryService.validateDuplicate(REF_CODE));
        verify(depositHistoryRepository, times(1)).existsByReferenceCode(REF_CODE);
    }

    @Test
    void 중복이_있으면_예외를_발생시킨다() {
        // given
        when(depositHistoryRepository.existsByReferenceCode(REF_CODE)).thenReturn(true);

        // when & then
        assertThrows(DuplicateDepositTransactionException.class,
                () -> depositHistoryService.validateDuplicate(REF_CODE));

        verify(depositHistoryRepository, times(1)).existsByReferenceCode(REF_CODE);
    }


    @Test
    void REFUND_코드_중복없고_원본코드가_존재하면_정상종료한다() {
        // given
        String refundCode = DepositHistoryServiceImpl.refundCode(REF_CODE);

        when(depositHistoryRepository.existsByReferenceCode(refundCode)).thenReturn(false); // refundCode 중복 X
        when(depositHistoryRepository.existsByReferenceCode(REF_CODE)).thenReturn(true);         // 원본 ref 존재

        // when & then
        assertDoesNotThrow(() -> depositHistoryService.validateDuplicateForRefund(REF_CODE));

        verify(depositHistoryRepository).existsByReferenceCode(refundCode);
        verify(depositHistoryRepository).existsByReferenceCode(REF_CODE);
    }

    @Test
    void REFUND_코드가_중복이면_DuplicateDepositTransactionException_을_던진다() {
        // given
        String refundCode = DepositHistoryServiceImpl.refundCode(REF_CODE);

        when(depositHistoryRepository.existsByReferenceCode(refundCode)).thenReturn(true);

        // when & then
        assertThrows(DuplicateDepositTransactionException.class,
                () -> depositHistoryService.validateDuplicateForRefund(REF_CODE));

        verify(depositHistoryRepository, times(1)).existsByReferenceCode(refundCode);
    }

    @Test
    void REFUND_코드_중복없고_원본코드가_없으면_RefundTargetNotFoundException_을_던진다() {
        // given
        String refundCode = DepositHistoryServiceImpl.refundCode(REF_CODE);

        when(depositHistoryRepository.existsByReferenceCode(refundCode)).thenReturn(false); // refundCode 중복 X
        when(depositHistoryRepository.existsByReferenceCode(REF_CODE)).thenReturn(false);        // 원본 ref 없음

        // when & then
        assertThrows(RefundTargetNotFoundException.class,
                () -> depositHistoryService.validateDuplicateForRefund(REF_CODE));

        verify(depositHistoryRepository).existsByReferenceCode(refundCode);
        verify(depositHistoryRepository).existsByReferenceCode(REF_CODE);
    }

    @Test
    void refundCode_정상적으로_prefix_가_붙는다() {
        // when
        String result = DepositHistoryServiceImpl.refundCode(REF_CODE);

        // then
        assertEquals("Refund-" + REF_CODE, result);
    }

    @Test
    void 실패히스토리가_정확한_값으로_save_된다() {
        // given
        DepositTransactionRequest request = new DepositTransactionRequest(AMOUNT, REF_CODE);
        Exception ex = new RuntimeException("test error");

        ArgumentCaptor<DepositHistory> captor = ArgumentCaptor.forClass(DepositHistory.class);

        // when
        depositHistoryService.saveFailedHistory(deposit, request, DepositHistoryType.CHARGE_FAILED, ex);

        // then
        verify(depositHistoryRepository, times(1)).save(captor.capture());
        DepositHistory saved = captor.getValue();

        assertEquals(AMOUNT, saved.getAmount());
        assertEquals(AMOUNT_SETUP, saved.getBalanceBefore());
        assertEquals(AMOUNT_SETUP, saved.getBalanceAfter());
        assertTrue(saved.getReferenceCode().contains(REF_CODE));
        assertTrue(saved.getReferenceCode().contains(ex.getClass().getSimpleName()));
        assertEquals(DepositHistoryType.CHARGE_FAILED, saved.getHistoryType());
    }
}