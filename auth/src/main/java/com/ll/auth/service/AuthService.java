package com.ll.auth.service;

import com.ll.auth.exception.DeviceCodeNotProvidedException;
import com.ll.auth.exception.TokenNotFoundException;
import com.ll.auth.exception.TokenNotProvidedException;
import com.ll.auth.model.entity.Auth;
import com.ll.auth.model.vo.dto.Tokens;
import com.ll.auth.model.vo.request.TokenValidRequest;
import com.ll.auth.oAuth2.JWTProvider;
import com.ll.auth.repository.AuthRepository;
import com.ll.user.model.vo.response.UserResponse;
import com.ll.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final UserService userService;
    private final AuthAsyncService authAsyncService;


    public List<Auth> findAllValid() {
        return authRepository.findByExpiredAtAfter((LocalDateTime.now()));
    }

    public Tokens refreshToken(TokenValidRequest request) {

        if (request.refreshToken() == null || request.refreshToken().isEmpty()) {
            throw new TokenNotProvidedException();
        }
        if (request.deviceCode() == null || request.deviceCode().isEmpty()) {
            throw new DeviceCodeNotProvidedException();
        }

        try {
            if (!redisService.validRefreshToken(request.refreshToken(), request.deviceCode())) {
                // 연속적인 Redis 갱신 보호
                if (!redisService.checkUpdateRedis() && authRepository.findByRefreshToken(request.refreshToken()).isPresent()) {
                    redisService.setUpdateRedisPending();
                    updateRedis();
                    redisService.setUpdateRedisCompleted();
                }
            }

            if (redisService.validRefreshToken(request.refreshToken(), request.deviceCode())) {
                String userCode = redisService.getUserCode(request.refreshToken(), request.deviceCode());
                UserResponse user = userService.getUserByUserCode(userCode);
                redisService.deleteRefreshToken(request.refreshToken(), request.deviceCode());
                return issuedToken(user.code(), request.deviceCode(), user.role().name());

            }

        } catch (Exception e) {
            log.error(e.getMessage());
            Optional<Auth> existing = authRepository.findByRefreshToken(request.refreshToken());
            if(existing.isPresent()) {
                Auth auth = existing.get();
                String userCode = auth.getUserCode();
                UserResponse user = userService.getUserByUserCode(userCode);
                return issuedToken(user.code(), request.deviceCode(), user.role().name());
            }
        }

        throw new TokenNotFoundException();
    }



    public Tokens issuedToken(String userCode,String deviceCode,String role){
        Tokens tokens = jWTProvider.createToken(userCode, role);
        try{
            redisService.saveRefreshToken(userCode, deviceCode, tokens.refreshToken());
            redisService.invalidateOldRefreshToken(userCode, deviceCode, tokens.refreshToken());
            redisService.saveUserDeviceMapping(userCode,deviceCode,tokens.refreshToken());
        }catch(Exception e){
            log.error(e.getMessage());
        }
        finally {
            authAsyncService.asyncUpsert(userCode, deviceCode, tokens.refreshToken());
        }
        return tokens;
    }

    public void logoutUser(String refreshToken , String deviceCode){
        try{
            redisService.deleteRefreshToken(refreshToken,deviceCode);
        }
        catch(Exception e){
            log.error(e.getMessage());
        }
        finally {
            authAsyncService.asyncDelete(refreshToken);
        }

    }

    private void updateRedis(){
        for (Auth auth : findAllValid()) {
            redisService.saveRefreshToken(auth.getUserCode(), auth.getDeviceCode(), auth.getRefreshToken());
        }
    }


}
