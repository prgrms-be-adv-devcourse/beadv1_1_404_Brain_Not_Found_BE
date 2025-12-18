package com.ll.payment.deposit;

import com.fasterxml.uuid.Generators;
import com.ll.payment.deposit.model.entity.Deposit;
import com.ll.payment.deposit.model.entity.DepositHistory;
import com.ll.payment.deposit.model.enums.DepositHistoryType;
import com.ll.payment.deposit.model.exception.DepositAmountMismatchException;
import com.ll.payment.deposit.model.exception.DuplicateDepositTransactionException;
import com.ll.payment.deposit.model.exception.RefundTargetNotFoundException;
import com.ll.payment.deposit.model.vo.request.DepositTransactionRequest;
import com.ll.payment.deposit.repository.DepositHistoryRepository;
import com.ll.payment.deposit.service.DepositHistoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"NonAsciiCharacters", "FieldCanBeLocal"})
@ExtendWith(MockitoExtension.class)
@DisplayName("DepositHistoryService 단위 테스트")
public class DepositHistoryServiceFailedUnitTest {

    @Mock
    private DepositHistoryRepository depositHistoryRepository;

    @InjectMocks
    private DepositHistoryServiceImpl depositHistoryService;

    private final String USER_CODE = "User_" + Generators.timeBasedGenerator().generate().toString();
    private final String REF_CODE = Generators.timeBasedGenerator().generate().toString();

    private final Long AMOUNT_SETUP = 1000L;

    private Deposit deposit;
    private DepositHistory depositChargeHistory;
    private DepositHistory depositPaymentHistory;
    private DepositTransactionRequest depositTransactionRequest;

    @BeforeEach
    void setUp() {
        deposit = Deposit.createInitialDeposit(USER_CODE);
        depositChargeHistory = deposit.charge(AMOUNT_SETUP, REF_CODE);
        depositPaymentHistory = deposit.payment(AMOUNT_SETUP, REF_CODE);
        depositTransactionRequest = new DepositTransactionRequest(AMOUNT_SETUP, REF_CODE);
    }

    @Test
    void 중복이_있으면_예외를_발생시킨다() {
        // given
        when(depositHistoryRepository.existsByReferenceCode(REF_CODE)).thenReturn(true);

        // when & then
        assertThrows(DuplicateDepositTransactionException.class,
                () -> depositHistoryService.validateDuplicate(depositTransactionRequest));

        verify(depositHistoryRepository, times(1)).existsByReferenceCode(REF_CODE);
    }

    @Test
    void REFUND_코드가_중복이면_DuplicateDepositTransactionException_을_던진다() {
        // given
        String refundCode = DepositHistoryServiceImpl.refundCode(REF_CODE);

        when(depositHistoryRepository.existsByReferenceCode(refundCode)).thenReturn(true);

        // when & then
        assertThrows(DuplicateDepositTransactionException.class,
                () -> depositHistoryService.validateDuplicateForRefund(depositTransactionRequest));

        verify(depositHistoryRepository, times(1)).existsByReferenceCode(refundCode);
    }

    @Test
    void REFUND_코드_중복없고_원본코드가_없으면_RefundTargetNotFoundException_을_던진다() {
        // given
        String refundCode = DepositHistoryServiceImpl.refundCode(REF_CODE);

        when(depositHistoryRepository.existsByReferenceCode(refundCode)).thenReturn(false);
        when(depositHistoryRepository.findByReferenceCode(REF_CODE)).thenReturn(Optional.empty());

        // when & then
        assertThrows(RefundTargetNotFoundException.class,
                () -> depositHistoryService.validateDuplicateForRefund(depositTransactionRequest));

        verify(depositHistoryRepository, times(1)).existsByReferenceCode(refundCode);
        verify(depositHistoryRepository, times(1)).findByReferenceCode(REF_CODE);
    }

    @Test
    void REFUND_코드_중복없고_원본코드가_결제내역이_아니면_RefundTargetNotFoundException_을_던진다() {
        // given
        String refundCode = DepositHistoryServiceImpl.refundCode(REF_CODE);

        when(depositHistoryRepository.existsByReferenceCode(refundCode)).thenReturn(false);
        when(depositHistoryRepository.findByReferenceCode(REF_CODE)).thenReturn(Optional.ofNullable(depositChargeHistory));

        // when & then
        assertThrows(RefundTargetNotFoundException.class,
                () -> depositHistoryService.validateDuplicateForRefund(depositTransactionRequest));

        verify(depositHistoryRepository, times(1)).existsByReferenceCode(refundCode);
        verify(depositHistoryRepository, times(1)).findByReferenceCode(REF_CODE);
        assertNotEquals(DepositHistoryType.PAYMENT, depositChargeHistory.getHistoryType());
    }

    @Test
    void REFUND_코드_중복없고_원본코드가_존재하지만_금액이_다르면_DepositAmountMismatchException_을_던진다() {
        // given
        String refundCode = DepositHistoryServiceImpl.refundCode(REF_CODE);
        DepositTransactionRequest wrongAmountRequest = new DepositTransactionRequest(AMOUNT_SETUP + 5000L, REF_CODE);

        when(depositHistoryRepository.existsByReferenceCode(refundCode)).thenReturn(false);
        when(depositHistoryRepository.findByReferenceCode(REF_CODE)).thenReturn(Optional.ofNullable(depositPaymentHistory));

        // when & then
        assertThrows(DepositAmountMismatchException.class,
                () -> depositHistoryService.validateDuplicateForRefund(wrongAmountRequest));

        verify(depositHistoryRepository, times(1)).existsByReferenceCode(refundCode);
        verify(depositHistoryRepository, times(1)).findByReferenceCode(REF_CODE);
        assertEquals(DepositHistoryType.PAYMENT, depositPaymentHistory.getHistoryType());
    }
}