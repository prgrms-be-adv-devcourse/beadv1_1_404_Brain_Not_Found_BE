package com.ll.payment.deposit;

import com.fasterxml.uuid.Generators;
import com.ll.payment.deposit.model.entity.Deposit;
import com.ll.payment.deposit.model.enums.DepositHistoryType;
import com.ll.payment.deposit.model.enums.DepositStatus;
import com.ll.payment.deposit.model.exception.*;
import com.ll.payment.deposit.model.vo.request.DepositDeleteRequest;
import com.ll.payment.deposit.model.vo.request.DepositTransactionRequest;
import com.ll.payment.deposit.repository.DepositRepository;
import com.ll.payment.deposit.service.DepositHistoryService;
import com.ll.payment.deposit.service.DepositServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"NonAsciiCharacters", "FieldCanBeLocal"})
@ExtendWith(MockitoExtension.class)
@DisplayName("DepositService 단위 테스트")
public class DepositServiceFailedUnitTest {

    @Mock
    private DepositRepository depositRepository;

    @Mock
    private DepositHistoryService depositHistoryService;

    @InjectMocks
    private DepositServiceImpl depositService;

    private final String USER_CODE = "User_" + Generators.timeBasedGenerator().generate().toString();
    private final String REF_CODE = Generators.timeBasedGenerator().generate().toString();
    private final String DELETE_REASON = "더 이상 해당 서비스를 이용하지 않습니다.";
    private final Long AMOUNT = 5000L;
    private final Long AMOUNT_EXISTING = 4000L;

    private Deposit deposit;
    private DepositTransactionRequest transactionRequest;
    private DepositDeleteRequest deleteRequest;

    @BeforeEach
    void setUp() {
        deposit = Deposit.createInitialDeposit(USER_CODE);
        transactionRequest = new DepositTransactionRequest(AMOUNT, REF_CODE);
        deleteRequest = new DepositDeleteRequest(DELETE_REASON);
    }

    /* ================
        Helper Methods
       ================ */

    private void mockDepositNotFound() {
        when(depositRepository.findByUserCode(USER_CODE))
                .thenReturn(Optional.empty());
    }

    private void mockDepositFound() {
        when(depositRepository.findByUserCode(USER_CODE))
                .thenReturn(Optional.of(deposit));
    }

    private void verifyFailedFlow(DepositHistoryType failedType) {
        verify(depositRepository).findByUserCode(USER_CODE);
        verify(depositHistoryService).validateDuplicate(transactionRequest);
        verify(depositHistoryService, never()).saveSuccessHistory(any());
        verify(depositHistoryService).saveFailedHistory(
                eq(deposit),
                eq(transactionRequest),
                eq(failedType),
                any(Exception.class)
        );
    }

    private void verifyRefundFailedFlow() {
        verify(depositRepository).findByUserCode(USER_CODE);
        verify(depositHistoryService).validateDuplicateForRefund(transactionRequest);
        verify(depositHistoryService, never()).saveSuccessHistory(any());
        verify(depositHistoryService).saveFailedHistory(
                eq(deposit),
                eq(transactionRequest),
                eq(DepositHistoryType.REFUND_FAILED),
                any(Exception.class)
        );
    }

    /* ==========================
        Deposit 생성 및 삭제 Tests
       ========================== */

    @Test
    void 기존_Deposit_이_Active_상태로_존재할_때_신규_Deposit_생성_시도_시_예외_발생() {
        // given
        mockDepositFound();

        // then
        assertThrows(
                DepositAlreadyExistsException.class,
                () -> depositService.createDeposit(USER_CODE)
        );

        verify(depositRepository).findByUserCode(USER_CODE);
        verify(depositRepository, never()).save(any());
    }

    @Test
    void 존재하지_않는_Deposit_을_삭제하려_할_때_예외_발생() {
        // given
        mockDepositNotFound();

        // then
        assertThrows(
                DepositNotFoundException.class,
                () -> depositService.deleteDepositByUserCode(USER_CODE, deleteRequest)
        );

        verify(depositRepository).findByUserCode(USER_CODE);
        verify(depositRepository, never()).save(any());
    }

    @Test
    void Inactive_상태인_Deposit_을_삭제하려_할_때_예외_발생() {
        // given
        deposit.setClosed();
        mockDepositFound();

        // then
        assertThrows(
                InvalidDepositStatusTransitionException.class,
                () -> depositService.deleteDepositByUserCode(USER_CODE, deleteRequest)
        );

        verify(depositRepository).findByUserCode(USER_CODE);
        verify(depositRepository, never()).save(any());
    }

    @Test
    void 잔액이_남아있는_Deposit_을_삭제하려_할_때_예외_발생() {
        // given
        deposit.charge(AMOUNT, REF_CODE);
        mockDepositFound();

        // then
        assertThrows(
                DepositBalanceNotEmptyException.class,
                () -> depositService.deleteDepositByUserCode(USER_CODE, deleteRequest)
        );

        verify(depositRepository).findByUserCode(USER_CODE);
        verify(depositRepository, never()).save(any());
        assertEquals(AMOUNT, deposit.getBalance());
        assertEquals(DepositStatus.ACTIVE, deposit.getDepositStatus());
    }

    /* ===================
        Deposit 충전 Tests
       =================== */

    @Test
    void Deposit_충전_실패_이미_존재하는_referenceCode() {
        // given
        mockDepositFound();

        doThrow(DuplicateDepositTransactionException.class)
                .when(depositHistoryService).validateDuplicate(transactionRequest);

        // then
        assertThrows(
                DuplicateDepositTransactionException.class,
                () -> depositService.chargeDeposit(USER_CODE, transactionRequest)
        );

        verifyFailedFlow(DepositHistoryType.CHARGE_FAILED);
        assertEquals(0L, deposit.getBalance());
    }

    @Test
    void InActive_상태의_Deposit_에_대한_충전_실패() {
        // given
        deposit.setClosed();
        mockDepositFound();
        doNothing().when(depositHistoryService).validateDuplicate(transactionRequest);

        // then
        assertThrows(
                InvalidDepositStatusTransitionException.class,
                () -> depositService.chargeDeposit(USER_CODE, transactionRequest)
        );

        verifyFailedFlow(DepositHistoryType.CHARGE_FAILED);
        assertEquals(0L, deposit.getBalance());
        assertEquals(DepositStatus.CLOSED, deposit.getDepositStatus());
    }

    @Test
    void 존재하지_않는_UserCode_로_출금_시도_시_예외_발생() {
        // given
        mockDepositNotFound();

        // then
        assertThrows(
                DepositNotFoundException.class,
                () -> depositService.withdrawDeposit(USER_CODE, transactionRequest)
        );

        verify(depositRepository).findByUserCode(USER_CODE);
    }

    /* ===================
        Deposit 출금 Tests
       =================== */

    @Test
    void Deposit_출금_실패_잔액_부족() {
        // given
        deposit.charge(AMOUNT_EXISTING, REF_CODE);
        mockDepositFound();
        doNothing().when(depositHistoryService).validateDuplicate(transactionRequest);
        // then
        assertThrows(
                InsufficientDepositBalanceException.class,
                () -> depositService.withdrawDeposit(USER_CODE, transactionRequest)
        );

        verifyFailedFlow(DepositHistoryType.WITHDRAW_FAILED);
        assertEquals(AMOUNT_EXISTING, deposit.getBalance());
    }

    @Test
    void Deposit_출금_실패_InActive_상태의_Deposit_에_대한_출금_시도() {
        // given
        deposit.setClosed();
        mockDepositFound();
        doNothing().when(depositHistoryService).validateDuplicate(transactionRequest);

        // then
        assertThrows(
                InvalidDepositStatusTransitionException.class,
                () -> depositService.withdrawDeposit(USER_CODE, transactionRequest)
        );

        verifyFailedFlow(DepositHistoryType.WITHDRAW_FAILED);
        assertEquals(0L, deposit.getBalance());
        assertEquals(DepositStatus.CLOSED, deposit.getDepositStatus());
    }

    @Test
    void Deposit_출금_실패_이미_존재하는_referenceCode() {
        // given
        deposit.charge(AMOUNT, REF_CODE);
        mockDepositFound();

        doThrow(DuplicateDepositTransactionException.class)
                .when(depositHistoryService).validateDuplicate(transactionRequest);

        // then
        assertThrows(
                DuplicateDepositTransactionException.class,
                () -> depositService.withdrawDeposit(USER_CODE, transactionRequest)
        );

        verifyFailedFlow(DepositHistoryType.WITHDRAW_FAILED);
        assertEquals(AMOUNT, deposit.getBalance());
    }

    @Test
    void 존재하지_않는_UserCode_로_충전_시도_시_예외_발생() {
        // given
        mockDepositNotFound();

        // then
        assertThrows(
                DepositNotFoundException.class,
                () -> depositService.chargeDeposit(USER_CODE, transactionRequest)
        );

        verify(depositRepository).findByUserCode(USER_CODE);
    }

    /* ===================
        Deposit 결제 Tests
       =================== */

    @Test
    void Deposit_결제_실패_잔액_부족() {
        // given
        deposit.charge(AMOUNT_EXISTING, REF_CODE);
        mockDepositFound();
        doNothing().when(depositHistoryService).validateDuplicate(transactionRequest);
        // then
        assertThrows(
                InsufficientDepositBalanceException.class,
                () -> depositService.paymentDeposit(USER_CODE, transactionRequest)
        );

        verifyFailedFlow(DepositHistoryType.PAYMENT_FAILED);
        assertEquals(AMOUNT_EXISTING, deposit.getBalance());
    }

    @Test
    void Deposit_결제_실패_InActive_상태의_Deposit_에_대한_결제_시도() {
        // given
        deposit.setClosed();
        mockDepositFound();
        doNothing().when(depositHistoryService).validateDuplicate(transactionRequest);

        // then
        assertThrows(
                InvalidDepositStatusTransitionException.class,
                () -> depositService.paymentDeposit(USER_CODE, transactionRequest)
        );

        verifyFailedFlow(DepositHistoryType.PAYMENT_FAILED);
        assertEquals(0L, deposit.getBalance());
        assertEquals(DepositStatus.CLOSED, deposit.getDepositStatus());
    }

    @Test
    void Deposit_결제_실패_이미_존재하는_referenceCode() {
        // given
        deposit.charge(AMOUNT, REF_CODE);
        mockDepositFound();
        doThrow(DuplicateDepositTransactionException.class)
                .when(depositHistoryService).validateDuplicate(transactionRequest);

        // then
        assertThrows(
                DuplicateDepositTransactionException.class,
                () -> depositService.paymentDeposit(USER_CODE, transactionRequest)
        );

        verifyFailedFlow(DepositHistoryType.PAYMENT_FAILED);
        assertEquals(AMOUNT, deposit.getBalance());
    }

    @Test
    void 존재하지_않는_UserCode_로_결제_시도_시_예외_발생() {
        // given
        mockDepositNotFound();

        // then
        assertThrows(
                DepositNotFoundException.class,
                () -> depositService.paymentDeposit(USER_CODE, transactionRequest)
        );

        verify(depositRepository).findByUserCode(USER_CODE);
    }

    /* ===================
        Deposit 환불 Tests
       =================== */

    @ParameterizedTest
    @ValueSource(classes = {
            DuplicateDepositTransactionException.class,
            RefundTargetNotFoundException.class
    })
    void 환불_실패시_공통_실패_처리_로직_검증(Class<? extends RuntimeException> ex) {
        // given
        mockDepositFound();
        doThrow(ex).when(depositHistoryService).validateDuplicateForRefund(transactionRequest);

        // when & then
        assertThrows(ex, () -> depositService.refundDeposit(USER_CODE, transactionRequest));

        verifyRefundFailedFlow();
        assertEquals(0L, deposit.getBalance());
    }

    @Test
    void Deposit_환불_실패_InActive_상태의_Deposit_에_대한_환불_시도() {
        // given
        deposit.setClosed();
        mockDepositFound();
        doNothing().when(depositHistoryService).validateDuplicateForRefund(transactionRequest);

        // then
        assertThrows(
                InvalidDepositStatusTransitionException.class,
                () -> depositService.refundDeposit(USER_CODE, transactionRequest)
        );

        verifyRefundFailedFlow();
        assertEquals(0L, deposit.getBalance());
        assertEquals(DepositStatus.CLOSED, deposit.getDepositStatus());
    }

    @Test
    void 존재하지_않는_UserCode_로_환불_시도_시_예외_발생() {
        // given
        mockDepositNotFound();

        // then
        assertThrows(
                DepositNotFoundException.class,
                () -> depositService.refundDeposit(USER_CODE, transactionRequest)
        );

        verify(depositRepository).findByUserCode(USER_CODE);
    }

}