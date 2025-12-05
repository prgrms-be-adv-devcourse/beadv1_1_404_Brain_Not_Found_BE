package com.ll.auth.service;

import com.ll.auth.model.entity.Auth;
import com.ll.auth.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthSyncService {

    private final AuthRepository authRepository;

    @Transactional
    public void upsert(String userCode, String deviceCode, String refreshToken) {
        authRepository.findByUserCodeAndDeviceCode(userCode, deviceCode)
                .ifPresentOrElse(
                        existing -> {
                            existing.updateRefreshToken(refreshToken);
                            authRepository.save(existing);
                        },
                        () -> authRepository.save(
                                Auth.builder()
                                        .userCode(userCode)
                                        .deviceCode(deviceCode)
                                        .refreshToken(refreshToken)
                                        .build()
                        )
                );
    }

    @Transactional
    public void delete(String refreshToken) {
        authRepository.deleteByRefreshToken(refreshToken);
    }
}
