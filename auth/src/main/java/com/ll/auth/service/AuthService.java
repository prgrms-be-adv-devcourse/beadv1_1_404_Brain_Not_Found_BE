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
import com.ll.user.model.vo.response.UserResponse;
import com.ll.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final UserService userService;
    private final AuthAsyncService authAsyncService;


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


        if (!redisService.validRefreshToken(request.refreshToken(),request.deviceCode())) {
            // 연속적인 Redis 갱신 보호
            if(!redisService.checkUpdateRedisLimit()){
                updateRedis();
                redisService.setUpdateRedisLimit();
            }
        }
        if (redisService.validRefreshToken(request.refreshToken(),request.deviceCode())) {
            String userCode = redisService.getUserCode(request.refreshToken(),request.deviceCode());
            UserResponse user = userService.getUserByUserCode(userCode);
            redisService.deleteRefreshToken(request.refreshToken(),request.deviceCode());
            issuedToken(user.code(),request.deviceCode(),user.role().name(),response);
            return;
        }
        throw new TokenNotFoundException();
    }

    public void issuedToken(String userCode,String deviceCode,String role,HttpServletResponse response){
        Tokens tokens = jWTProvider.createToken(userCode, role);
        redisService.saveRefreshToken(userCode, deviceCode, tokens.refreshToken());
        redisService.invalidateOldRefreshToken(userCode, deviceCode, tokens.refreshToken());
        redisService.saveUserDeviceMapping(userCode,deviceCode,tokens.refreshToken());
        authAsyncService.asyncSave(userCode, deviceCode, tokens.refreshToken());
        CookieUtil.setTokenCookie(response,tokens.accessToken(),tokens.refreshToken());
    }

    public void logoutUser(String refreshToken , HttpServletResponse response , String deviceCode){
        redisService.deleteRefreshToken(refreshToken,deviceCode);
        authAsyncService.asyncDelete(refreshToken);
        CookieUtil.expiredCookie(response);
    }

    private void updateRedis(){
        for (Auth auth : findAllValid()) {
            redisService.saveRefreshToken(auth.getUserCode(), auth.getDeviceCode(), auth.getRefreshToken());
        }
    }

}
