package com.ll.auth.service;

import com.ll.auth.exception.TokenNotFoundException;
import com.ll.auth.model.entity.Auth;
import com.ll.auth.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthAsyncService {

    private final AuthRepository authRepository;

    public void save(Auth auth){
        authRepository.save(auth);
    }

    public void update(String userCode, String deviceCode, String refreshToken){
        Auth auth = authRepository.findByUserCodeAndDeviceCode(userCode,deviceCode).orElseThrow(TokenNotFoundException::new);
        auth.updateRefreshToken(refreshToken);
        save(auth);
    }

    public void delete(String refreshToken){
        authRepository.deleteByRefreshToken(refreshToken);
    }

    @Async
    @Transactional
    public void asyncDelete(String refreshToken){
        delete(refreshToken);
        log.info("Async DB delete 완료: refreshToken={}", refreshToken);
    }

    @Async
    @Transactional
    public void asyncSave(String userCode, String deviceCode, String refreshToken){

        Optional<Auth> auth = authRepository.findByUserCodeAndDeviceCode(userCode,deviceCode);

        if(auth.isPresent()){
            update(userCode, deviceCode, refreshToken);
        }
        else{
            save(Auth.builder()
                    .userCode(userCode)
                    .deviceCode(deviceCode)
                    .refreshToken(refreshToken)
                    .build());
        }
        log.info("Async DB Save 완료");
    }
}
