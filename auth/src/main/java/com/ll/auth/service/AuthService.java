package com.ll.auth.service;

import com.ll.auth.exception.DeviceCodeNotProvidedException;
import com.ll.auth.exception.TokenNotFoundException;
import com.ll.auth.exception.TokenNotProvidedException;
import com.ll.auth.model.entity.Auth;
import com.ll.auth.model.vo.dto.Tokens;
import com.ll.auth.model.vo.request.TokenValidRequest;
import com.ll.auth.oAuth2.JWTProvider;
import com.ll.auth.repository.AuthRepository;
import com.ll.auth.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthRepository authRepository;
    private final JWTProvider jWTProvider;
    private final RedisService redisService;

    public void save(Auth auth){
        authRepository.save(auth);
    }

    public void update(String userCode, String deviceCode, String refreshToken){
        Auth auth = authRepository.findByUserCodeAndDeviceCode(userCode,deviceCode).orElseThrow(TokenNotFoundException::new);
        auth.updateRefreshToken(refreshToken);
        save(auth);
    }

    public void delete(String userCode, String deviceCode){
        authRepository.deleteByUserCodeAndDeviceCode(userCode,deviceCode);
    }

    public List<Auth> findAllValid(){
        return authRepository.findByExpiredAtAfter((LocalDateTime.now()));
    }

    public void refreshToken(TokenValidRequest request,HttpServletResponse response){

        if(request.refreshToken() == null || request.refreshToken().isEmpty()){
            throw new TokenNotProvidedException();
        }
        if(request.deviceCode() == null || request.deviceCode().isEmpty()){
            throw new DeviceCodeNotProvidedException();
        }

        String storedToken = redisService.getRefreshToken(request.userCode(), request.deviceCode());
        if (!request.refreshToken().equals(storedToken)) {
            // 연속적인 Redis 갱신 보호
            if(!redisService.checkUpdateRedisLimit()){
                updateRedis();
                redisService.setUpdateRedisLimit();
            }
            storedToken = redisService.getRefreshToken(request.userCode(), request.deviceCode());
        }
        if (request.refreshToken().equals(storedToken)) {
            issuedToken(request.userCode(),request.deviceCode(),request.role(),response);
            return;
        }
        throw new TokenNotFoundException();
    }
    public void issuedToken(String userCode,String deviceCode,String role,HttpServletResponse response){

        Tokens tokens = jWTProvider.createToken(userCode, role);
        redisService.saveRefreshToken(userCode, deviceCode, tokens.refreshToken());
        asyncUpdate(userCode, deviceCode, tokens.refreshToken());
        CookieUtil.setTokenCookie(response,tokens.accessToken(),tokens.refreshToken());

    }

    public void logoutUser(String userCode , HttpServletResponse response , String deviceCode){
        redisService.deleteRefreshToken(userCode,deviceCode);
        asyncDelete(userCode,deviceCode);
        CookieUtil.expiredCookie(response);
    }

    private void updateRedis(){
        for (Auth auth : findAllValid()) {
            redisService.saveRefreshToken(auth.getUserCode(), auth.getDeviceCode(), auth.getRefreshToken());
        }
    }

    @Async
    public void asyncUpdate(String userCode, String deviceCode, String refreshToken){
        update(userCode, deviceCode, refreshToken);
        log.info("Async DB update 완료: userCode={}, deviceCode={}", userCode, deviceCode);
    }

    @Async
    public void asyncDelete(String userCode, String deviceCode){
        delete(userCode, deviceCode);
        log.info("Async DB delete 완료: userCode={}, deviceCode={}", userCode, deviceCode);
    }

    @Async
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
