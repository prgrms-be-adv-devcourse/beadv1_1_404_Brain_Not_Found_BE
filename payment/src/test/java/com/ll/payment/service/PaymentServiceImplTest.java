package com.ll.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ll.payment.client.DepositServiceClient;
import com.ll.payment.model.dto.DepositInfoResponse;
import com.ll.payment.model.dto.PaymentProcessResult;
import com.ll.payment.model.entity.Payment;
import com.ll.payment.model.enums.PaidType;
import com.ll.payment.model.enums.PaymentStatus;
import com.ll.payment.model.vo.PaymentRequest;
import com.ll.payment.repository.PaymentJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService 테스트")
class PaymentServiceImplTest {

    @Mock
    private PaymentJpaRepository paymentJpaRepository;

    @Mock
    private RestClient restClient;

    @Mock
    private DepositServiceClient depositServiceClient;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @DisplayName("예치금이 충분하면 예치금으로만 결제 처리")
    @Test
    void depositPayment_withSufficientBalance() {
        PaymentServiceImpl service = createServiceSpy();
        PaymentRequest paymentRequest = new PaymentRequest(
                1L,
                2L,
                "USER-001",
                5_000,
                PaidType.DEPOSIT,
                "PAY-KEY"
        );

        when(depositServiceClient.getDepositInfo("USER-001"))
                .thenReturn(new DepositInfoResponse("USER-001", 7_000L));
        when(paymentJpaRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<String> referenceCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);

        PaymentProcessResult result = service.depositPayment(paymentRequest);
        assertThat(result).isNotNull();

        verify(depositServiceClient).withdraw(eq("USER-001"), eq(5_000L), referenceCaptor.capture());
        assertThat(referenceCaptor.getValue()).startsWith("ORDER-1-");

        verify(service, never()).tossPayment(any(PaymentRequest.class));

        verify(paymentJpaRepository, times(1)).save(paymentCaptor.capture());
        Payment savedPayment = paymentCaptor.getValue();
        assertThat(savedPayment.getOrderId()).isEqualTo(1L);
        assertThat(savedPayment.getBuyerId()).isEqualTo(2L);
        assertThat(savedPayment.getPaidAmount()).isEqualTo(5_000);
        assertThat(savedPayment.getPaidType()).isEqualTo(PaidType.DEPOSIT);
        assertThat(savedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(result.depositPayment()).isSameAs(savedPayment);
        assertThat(result.tossPayment()).isNull();
    }

    @DisplayName("예치금이 일부만 있을 때 잔액만큼 예치금 결제 후 토스 결제를 진행")
    @Test
    void depositPayment_withPartialBalance() {
        PaymentServiceImpl service = createServiceSpy();
        PaymentRequest paymentRequest = new PaymentRequest(
                1L,
                2L,
                "USER-001",
                5_000,
                PaidType.DEPOSIT,
                "PAY-KEY"
        );

        when(depositServiceClient.getDepositInfo("USER-001"))
                .thenReturn(new DepositInfoResponse("USER-001", 3_000L));
        when(paymentJpaRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Payment tossResult = mock(Payment.class);
        doReturn(tossResult).when(service).tossPayment(any(PaymentRequest.class));

        ArgumentCaptor<String> referenceCaptor = ArgumentCaptor.forClass(String.class);
        // 토스 결제 요청이 어떤 PaymentRequest로 호출되었는지 검증하기 위한 캡처
        ArgumentCaptor<PaymentRequest> tossRequestCaptor = ArgumentCaptor.forClass(PaymentRequest.class);
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);

        PaymentProcessResult result = service.depositPayment(paymentRequest);
        assertThat(result).isNotNull();

        verify(depositServiceClient).withdraw(eq("USER-001"), eq(3_000L), referenceCaptor.capture());
        assertThat(referenceCaptor.getValue()).startsWith("ORDER-1-");

        verify(paymentJpaRepository, times(1)).save(paymentCaptor.capture());
        Payment savedPayment = paymentCaptor.getValue();
        assertThat(savedPayment.getPaidAmount()).isEqualTo(3_000);
        assertThat(savedPayment.getPaidType()).isEqualTo(PaidType.DEPOSIT);
        assertThat(result.depositPayment()).isSameAs(savedPayment);

        verify(service).tossPayment(tossRequestCaptor.capture());
        PaymentRequest tossRequest = tossRequestCaptor.getValue();
        assertThat(tossRequest.paidAmount()).isEqualTo(2_000);
        assertThat(tossRequest.paidType()).isEqualTo(PaidType.TOSS_PAYMENT);
        assertThat(tossRequest.orderId()).isEqualTo(paymentRequest.orderId());
        assertThat(tossRequest.buyerId()).isEqualTo(paymentRequest.buyerId());
        assertThat(tossRequest.buyerCode()).isEqualTo(paymentRequest.buyerCode());
        assertThat(tossRequest.paymentKey()).isEqualTo(paymentRequest.paymentKey());
        assertThat(result.tossPayment()).isSameAs(tossResult);
    }

    @DisplayName("예치금이 없으면 토스 결제만 진행")
    @Test
    void depositPayment_withoutBalance() {
        PaymentServiceImpl service = createServiceSpy();
        PaymentRequest paymentRequest = new PaymentRequest(
                1L,
                2L,
                "USER-001",
                5_000,
                PaidType.DEPOSIT,
                "PAY-KEY"
        );

        when(depositServiceClient.getDepositInfo("USER-001"))
                .thenReturn(new DepositInfoResponse("USER-001", 0L));
        Payment tossResult = mock(Payment.class);
        doReturn(tossResult).when(service).tossPayment(any(PaymentRequest.class));

        ArgumentCaptor<PaymentRequest> tossRequestCaptor = ArgumentCaptor.forClass(PaymentRequest.class);

        PaymentProcessResult result = service.depositPayment(paymentRequest);
        assertThat(result).isNotNull();

        verify(depositServiceClient, never()).withdraw(anyString(), anyLong(), anyString());
        verify(paymentJpaRepository, never()).save(any(Payment.class));

        verify(service).tossPayment(tossRequestCaptor.capture());
        PaymentRequest tossRequest = tossRequestCaptor.getValue();
        assertThat(tossRequest.paidAmount()).isEqualTo(5_000);
        assertThat(tossRequest.paidType()).isEqualTo(PaidType.TOSS_PAYMENT);
        assertThat(result.depositPayment()).isNull();
        assertThat(result.tossPayment()).isSameAs(tossResult);
    }

    private PaymentServiceImpl createServiceSpy() {
        PaymentServiceImpl service = new PaymentServiceImpl(
                paymentJpaRepository,
                restClient,
                objectMapper,
                depositServiceClient
        );
        setField(service, "secretKey", "test-secret");
        setField(service, "targetUrl", "http://test-url");
        return spy(service);
    }

    private void setField(PaymentServiceImpl service, String fieldName, Object value) {
        try {
            Field field = PaymentServiceImpl.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(service, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

