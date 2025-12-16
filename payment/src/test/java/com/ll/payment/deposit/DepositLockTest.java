package com.ll.payment.deposit;

import com.ll.payment.deposit.model.entity.Deposit;
import com.ll.payment.deposit.repository.DepositRepository;
import com.ll.payment.support.MySQLTestContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

// Lock 은 실제 DB 에서만 기능하기 때문에 Mock 을 사용하지 않은 통합 테스트로 작성합니다.
@SpringBootTest
@ActiveProfiles("ci-test")
@DisplayName("findByUserCode 에 걸린 Lock 검증 테스트")
public class DepositLockTest extends MySQLTestContainer {

    @Autowired
    private DepositRepository depositRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;
    private final String USER_1 = "user1";
    private final Long MS_TO_WAIT = 2000L;
    private Long WAITED = null;

    @Test
    void pessimisticLock_should_block_second_transaction() throws Exception {
        depositRepository.save(Deposit.createInitialDeposit(USER_1));

        CountDownLatch latch = new CountDownLatch(1);

        Thread t1 = new Thread(t1Task(latch));
        Thread t2 = new Thread(t2Task(latch));

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        // WAITED 가 대략 MS_TO_WAIT 이상이어야 함을 검사 assert
        assertThat(WAITED).isNotNull();
        assertThat(WAITED).isGreaterThanOrEqualTo(MS_TO_WAIT);

    }


    // ---------------------- 스레드 작업 --------------------- //
    private Runnable t1Task(CountDownLatch latch) {
        return () -> runInTransaction(() -> {
            findByUserCodeWithLock();
            System.out.println("T1: 락 획득");
            latch.countDown();
            sleep(MS_TO_WAIT);
        });
    }

    private Runnable t2Task(CountDownLatch latch) {
        return () -> runInTransaction(() -> {
            waitLatch(latch);
            System.out.println("T2: 락 시도");
            WAITED = measure(this::findByUserCodeWithLock);
            System.out.println("T2: 락 획득됨 (대기 시간: " + WAITED + "ms)");
            // Repository 에서 Lock 을 지우면 바로 Lock 획득
        });
    }


    // ---------------------- 트랜잭션 템플릿 메서드 --------------------- //
    private void runInTransaction(Runnable runnable) {
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        tt.execute(status -> {
            runnable.run();
            return null;
        });
    }


    // ---------------------- 유틸리티 메서드 --------------------- //
    private long measure(Runnable action) {
        long start = System.currentTimeMillis();
        action.run();
        return System.currentTimeMillis() - start;
    }

    private void waitLatch(CountDownLatch latch) {
        try { latch.await(); } catch (InterruptedException ignored) {}
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    private void findByUserCodeWithLock() {
        depositRepository.findByUserCode(USER_1);
    }
}
