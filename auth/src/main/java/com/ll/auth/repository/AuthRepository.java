package com.ll.auth.repository;

import com.ll.auth.model.entity.Auth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuthRepository extends JpaRepository<Auth, Long> {

    Optional<Auth> findByUserCode(String userCode);

    Optional<Auth> findByUserCodeAndDeviceCode(String userCode, String deviceCode);

    List<Auth> findByExpiredAtAfter(LocalDateTime expiredAtAfter);

    void deleteByRefreshToken(String refreshToken);
}
