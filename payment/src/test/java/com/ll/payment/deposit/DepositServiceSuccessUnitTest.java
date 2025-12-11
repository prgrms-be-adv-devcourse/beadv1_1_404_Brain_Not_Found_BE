package com.ll.payment.deposit;

import com.fasterxml.uuid.Generators;
import com.ll.payment.deposit.model.entity.Deposit;
import com.ll.payment.deposit.model.enums.DepositHistoryType;
import com.ll.payment.deposit.model.enums.DepositStatus;
import com.ll.payment.deposit.model.vo.request.DepositDeleteRequest;
import com.ll.payment.deposit.model.vo.request.DepositTransactionRequest;
import com.ll.payment.deposit.model.vo.response.DepositDeleteResponse;
import com.ll.payment.deposit.model.vo.response.DepositResponse;
import com.ll.payment.deposit.model.vo.response.DepositTransactionResponse;
import com.ll.payment.deposit.repository.DepositRepository;
import com.ll.payment.deposit.service.DepositHistoryService;
import com.ll.payment.deposit.service.DepositHistoryServiceImpl;
import com.ll.payment.deposit.service.DepositServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
public class DepositServiceSuccessUnitTest {

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

    private Deposit deposit;
    private DepositTransactionRequest transactionRequest;
    private DepositTransactionResponse transactionResponse;
    private DepositDeleteRequest deleteRequest;
    private DepositDeleteResponse deleteResponse;

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

    private void verifySuccessFlow() {
        verify(depositRepository).findByUserCode(USER_CODE);
        verify(depositHistoryService).validateDuplicate(transactionRequest);
        verify(depositHistoryService).saveSuccessHistory(any());
        verify(depositHistoryService, never()).saveFailedHistory(any(), any(), any(), any());
    }

    private void verifyRefundSuccessFlow() {
        verify(depositRepository).findByUserCode(USER_CODE);
        verify(depositHistoryService).validateDuplicateForRefund(transactionRequest);
        verify(depositHistoryService).saveSuccessHistory(any());
        verify(depositHistoryService, never()).saveFailedHistory(any(), any(), any(), any());
    }

    private void assertTransactionResponse(Long balance, DepositHistoryType charge) {
        assertNotNull(transactionResponse);
        assertEquals(deposit.getBalance(), balance);
        assertEquals(deposit.getCode(), transactionResponse.depositCode());
        assertEquals(AMOUNT, transactionResponse.amount());
        assertEquals(balance, transactionResponse.balanceAfter());
        assertEquals(charge, transactionResponse.historyType());
        assertEquals(REF_CODE, transactionResponse.referenceCode());
    }

    private void assertTransactionResponseRefund(Long balance) {
        assertNotNull(transactionResponse);
        assertEquals(deposit.getBalance(), balance);
        assertEquals(deposit.getCode(), transactionResponse.depositCode());
        assertEquals(AMOUNT, transactionResponse.amount());
        assertEquals(balance, transactionResponse.balanceAfter());
        assertEquals(DepositHistoryType.REFUND, transactionResponse.historyType());
        assertEquals(DepositHistoryServiceImpl.refundCode(REF_CODE), transactionResponse.referenceCode());
    }

    private void assertDeleteResponse() {
        verify(depositRepository).findByUserCode(USER_CODE);
        verify(depositRepository).save(argThat(d -> d.getDepositStatus() == DepositStatus.CLOSED));
        assertNotNull(deleteResponse);
        assertEquals(USER_CODE, deleteResponse.userCode());
        assertEquals(DELETE_REASON, deleteResponse.closedReason());
        assertEquals(DepositStatus.CLOSED, deposit.getDepositStatus());
        assertEquals(DepositStatus.CLOSED, deleteResponse.depositStatus());
    }

    /* ==========================
        Deposit 생성 및 삭제 Tests
       ========================== */

    @Test
    void 기존_Deposit_이_존재하지_않을_때_신규_Deposit_을_생성() {
        // given
        mockDepositNotFound();
        when(depositRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // when
        DepositResponse response = depositService.createDeposit(USER_CODE);

        // then
        verify(depositRepository).findByUserCode(USER_CODE);
        verify(depositRepository).save(argThat(d -> d.getDepositStatus() == DepositStatus.ACTIVE));
        assertNotNull(response);
        assertEquals(USER_CODE, response.userCode());
    }

    @Test
    void 기존_Deposit_이_Close_상태로_존재할_때_해당_Deposit_상태를_Active_로_변경() {
        // given
        deposit.setClosed();
        mockDepositFound();

        // when
        DepositResponse response = depositService.createDeposit(USER_CODE);

        // then
        verify(depositRepository).findByUserCode(USER_CODE);
        assertNotNull(response);
        assertEquals(USER_CODE, response.userCode());
        assertEquals(DepositStatus.ACTIVE, deposit.getDepositStatus());
    }

    @Test
    void Deposit_의_상태를_Close_로_변경() {
        // given
        mockDepositFound();
        when(depositRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // when
        deleteResponse = depositService.deleteDepositByUserCode(USER_CODE, deleteRequest);

        // then
        assertDeleteResponse();
    }

    /* ===================
        Deposit 충전 Tests
       =================== */

    @Test
    void Deposit_충전_성공() {
        // given
        mockDepositFound();
        doNothing().when(depositHistoryService).validateDuplicate(transactionRequest);
        doNothing().when(depositHistoryService).saveSuccessHistory(any());

        // when
        transactionResponse = depositService.chargeDeposit(USER_CODE, transactionRequest);

        // then
        verifySuccessFlow();
        assertTransactionResponse(AMOUNT, DepositHistoryType.CHARGE);
    }

    /* ===================
        Deposit 출금 Tests
       =================== */

    @Test
    void Deposit_출금_성공() {
        // given
        deposit.charge(AMOUNT, REF_CODE);
        mockDepositFound();
        doNothing().when(depositHistoryService).validateDuplicate(transactionRequest);
        doNothing().when(depositHistoryService).saveSuccessHistory(any());
        // when
        transactionResponse = depositService.withdrawDeposit(USER_CODE, transactionRequest);

        // then
        verifySuccessFlow();
        assertTransactionResponse(0L, DepositHistoryType.WITHDRAW);
    }

    /* ===================
        Deposit 결제 Tests
       =================== */

    @Test
    void Deposit_결제_성공() {
        // given
        deposit.charge(AMOUNT, REF_CODE);
        mockDepositFound();
        doNothing().when(depositHistoryService).validateDuplicate(transactionRequest);
        doNothing().when(depositHistoryService).saveSuccessHistory(any());

        // when
        transactionResponse = depositService.paymentDeposit(USER_CODE, transactionRequest);

        // then
        verifySuccessFlow();
        assertTransactionResponse(0L, DepositHistoryType.PAYMENT);
    }

    /* ===================
        Deposit 환불 Tests
       =================== */

    @Test
    void Deposit_환불_성공() {
        // given
        mockDepositFound();
        doNothing().when(depositHistoryService).validateDuplicateForRefund(transactionRequest);
        doNothing().when(depositHistoryService).saveSuccessHistory(any());
        // when
        transactionResponse = depositService.refundDeposit(USER_CODE, transactionRequest);

        // then
        verifyRefundSuccessFlow();
        assertTransactionResponseRefund(AMOUNT);
    }
}