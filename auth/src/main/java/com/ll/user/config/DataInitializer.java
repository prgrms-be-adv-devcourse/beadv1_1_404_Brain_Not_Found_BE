package com.ll.user.config;

import com.ll.user.model.entity.User;
import com.ll.user.model.enums.AccountStatus;
import com.ll.user.model.enums.Grade;
import com.ll.user.model.enums.Role;
import com.ll.user.model.enums.SocialProvider;
import com.ll.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!prod") // 프로덕션 환경에서는 실행되지 않도록
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("더미 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        log.info("더미 사용자 데이터를 생성합니다...");

        // 테스트용 사용자 1
        User user1 = User.builder()
                .socialId("test_social_id_1")
                .email("user1@test.com")
                .name("홍길동")
                .socialProvider(SocialProvider.KAKAO)
                .role(Role.USER)
                .mannerScore(5L)
                .grade(Grade.BRONZE)
                .accountStatus(AccountStatus.ACTIVE)
                .address("서울시 강남구 테헤란로 123")
                .build();
        userRepository.save(user1);
        log.info("사용자 생성 완료: {} (code: {})", user1.getName(), user1.getCode());

        // 테스트용 사용자 2
        User user2 = User.builder()
                .socialId("test_social_id_2")
                .email("user2@test.com")
                .name("김철수")
                .socialProvider(SocialProvider.KAKAO)
                .role(Role.USER)
                .mannerScore(4L)
                .grade(Grade.SILVER)
                .accountStatus(AccountStatus.ACTIVE)
                .address("서울시 서초구 서초대로 456")
                .build();
        userRepository.save(user2);
        log.info("사용자 생성 완료: {} (code: {})", user2.getName(), user2.getCode());

        // 테스트용 사용자 3 (판매자)
        User seller1 = User.builder()
                .socialId("test_social_id_3")
                .email("seller1@test.com")
                .name("판매자1")
                .socialProvider(SocialProvider.KAKAO)
                .role(Role.USER)
                .mannerScore(5L)
                .grade(Grade.GOLD)
                .accountStatus(AccountStatus.ACTIVE)
                .address("서울시 마포구 홍대입구로 789")
                .accountBank("카카오뱅크")
                .accountNumber("1234-5678-9012")
                .build();
        userRepository.save(seller1);
        log.info("판매자 생성 완료: {} (code: {})", seller1.getName(), seller1.getCode());

        log.info("더미 사용자 데이터 생성 완료. 총 {}명", userRepository.count());
    }
}

