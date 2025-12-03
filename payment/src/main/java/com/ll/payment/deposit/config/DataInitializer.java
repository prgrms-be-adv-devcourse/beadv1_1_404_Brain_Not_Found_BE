package com.ll.payment.deposit.config;

import com.ll.payment.deposit.client.UserServiceClient;
import com.ll.payment.deposit.model.entity.Deposit;
import com.ll.payment.deposit.model.vo.response.UserInfoResponse;
import com.ll.payment.deposit.repository.DepositRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!prod") // 프로덕션 환경에서는 실행되지 않도록
public class DataInitializer implements CommandLineRunner {

    private final DepositRepository depositRepository;
    private final UserServiceClient userServiceClient;
    private final RestClient restClient;

    @Value("${external.user-service.url:http://localhost:8080}")
    private String userServiceUrl;

    @Override
    public void run(String... args) {
        log.info("더미 예치금 데이터를 생성합니다...");

        try {
            // User 서비스에서 모든 사용자 목록 가져오기
            List<UserInfoResponse> users = getAllUsers();
            
            if (users == null || users.isEmpty()) {
                log.warn("User 서비스에서 사용자 목록을 가져올 수 없습니다. 하드코딩된 userCode를 사용합니다.");
                createDepositWithHardcodedUserCode();
                return;
            }

            // 각 사용자에 대해 예치금 계좌 생성
            for (int i = 0; i < Math.min(users.size(), 3); i++) {
                UserInfoResponse user = users.get(i);
                String userCode = user.userCode();
                
                if (depositRepository.findByUserCode(userCode).isEmpty()) {
                    Deposit deposit = Deposit.createInitialDeposit(userCode);
                    
                    // 첫 번째 사용자: 10만원, 두 번째: 5천원, 세 번째: 0원
                    if (i == 0) {
                        deposit.charge(100000L, "InitialCharge-100000");
                    } else if (i == 1) {
                        deposit.charge(5000L, "InitialCharge-5000");
                    }
                    
                    depositRepository.save(deposit);
                    log.info("예치금 계좌 생성 완료: userCode={}, balance={}", userCode, deposit.getBalance());
                } else {
                    log.info("예치금 계좌가 이미 존재합니다: userCode={}", userCode);
                }
            }
        } catch (Exception e) {
            log.error("User 서비스 호출 실패, 하드코딩된 userCode 사용: {}", e.getMessage());
            createDepositWithHardcodedUserCode();
        }

        log.info("더미 예치금 데이터 생성 완료. 총 {}개", depositRepository.count());
    }

    private List<UserInfoResponse> getAllUsers() {
        try {
            // User 서비스의 사용자 목록 API 호출 (실제 엔드포인트에 맞게 수정 필요)
            // 여기서는 예시로 단일 사용자 조회를 여러 번 호출하는 방식 사용
            // 실제로는 User 서비스에 목록 조회 API가 있어야 함
            return null; // TODO: User 서비스의 사용자 목록 API 구현 필요
        } catch (Exception e) {
            log.error("사용자 목록 조회 실패", e);
            return null;
        }
    }

    private void createDepositWithHardcodedUserCode() {
        // 하드코딩된 userCode 사용 (User 모듈 실행 후 로그에서 확인한 실제 code로 변경 필요)
        String userCode1 = "019a90ab-fcf3-7413-af08-7121cc99378b"; // 첫 번째 사용자의 실제 code
        if (depositRepository.findByUserCode(userCode1).isEmpty()) {
            Deposit deposit1 = Deposit.createInitialDeposit(userCode1);
            deposit1.charge(100000L, "InitialCharge-100000");
            depositRepository.save(deposit1);
            log.info("예치금 계좌 생성 완료: userCode={}, balance={}", userCode1, deposit1.getBalance());
        }
    }
}

