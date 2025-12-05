package com.ll.order.domain.mock.controller;

import com.ll.core.model.response.BaseResponse;
import com.ll.order.domain.model.enums.user.AccountStatus;
import com.ll.order.domain.model.enums.user.Grade;
import com.ll.order.domain.model.enums.user.Role;
import com.ll.order.domain.model.enums.user.SocialProvider;
import com.ll.order.domain.model.vo.response.user.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * User Service Mock Controller
 * 로컬 개발 환경에서 사용자 정보를 모킹하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserMockController {

    /**
     * 사용자 정보 조회 Mock API
     * GET /api/users/info
     * Header: X-User-Code
     */
    @GetMapping("/info")
    public ResponseEntity<BaseResponse<UserResponse>> getUserInfo(
            @RequestHeader("X-User-Code") String userCode
    ) {
        log.info("Mock User Service - 사용자 조회 요청: userCode={}", userCode);

        // USER-001에 대한 더미 데이터 반환
        if ("USER-001".equals(userCode)) {
            UserResponse userResponse = new UserResponse(
                    1L,
                    "USER-001",
                    "mock_social_id_001",
                    SocialProvider.KAKAO,
                    "user001@example.com",
                    "홍길동",
                    Role.USER,
                    "https://example.com/profile/user001.jpg",
                    100L,
                    Grade.BRONZE,
                    AccountStatus.ACTIVE,
                    "KB",
                    "123-456-789",
                    LocalDateTime.of(2024, 1, 1, 0, 0, 0),
                    LocalDateTime.of(2024, 1, 1, 0, 0, 0)
            );

            log.info("Mock User Service - 사용자 조회 성공: userCode={}", userCode);
            return BaseResponse.ok(userResponse);
        }

        // 존재하지 않는 사용자
        log.warn("Mock User Service - 사용자를 찾을 수 없음: userCode={}", userCode);
        return BaseResponse.error(com.ll.core.model.exception.ErrorCode.NOT_FOUND);
    }
}

