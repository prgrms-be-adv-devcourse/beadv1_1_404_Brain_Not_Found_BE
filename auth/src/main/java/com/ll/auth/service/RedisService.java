package com.ll.auth.service;

import com.ll.auth.exception.TokenNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public void saveRefreshToken(String userCode, String deviceCode , String refreshToken) {
        redisTemplate.opsForValue().set(generateRefreshCode(refreshToken,deviceCode),userCode,Duration.ofDays(7));
    }

    public void saveUserDeviceMapping(String userCode, String deviceCode, String refreshToken) {
        String key = generateUserCode(userCode, deviceCode);
        redisTemplate.opsForValue().set(key, refreshToken);
    }

    public void invalidateOldRefreshToken(String userCode, String deviceCode, String newRefreshToken) {

        String key = generateUserCode(userCode, deviceCode);
        String oldRefreshToken = redisTemplate.opsForValue().get(key);

        if (oldRefreshToken != null && !oldRefreshToken.equals(newRefreshToken)) {
            redisTemplate.delete("refresh:" + oldRefreshToken + ":" + deviceCode);
        }
    }

    public boolean validRefreshToken(String refreshToken,String deviceCode) {
        return redisTemplate.hasKey(generateRefreshCode(refreshToken,deviceCode));
    }

    public String getUserCode(String refreshToken, String deviceCode) {
        String value = redisTemplate.opsForValue().get(generateRefreshCode(refreshToken,deviceCode));
        return Optional.ofNullable(value).orElseThrow(TokenNotFoundException::new);
    }

    public void deleteRefreshToken(String refreshToken, String deviceCode) {
        if(!redisTemplate.hasKey(generateRefreshCode(refreshToken,deviceCode))) {
            return;
        }
        redisTemplate.delete(generateRefreshCode(refreshToken,deviceCode));
    }


    // 연속적인 Redis 갱신 보호

    public void setUpdateRedisLimit(){
        redisTemplate.opsForValue().set("refresh:update","1",Duration.ofMinutes(1));
    }

    public boolean checkUpdateRedisLimit(){
        return redisTemplate.hasKey("refresh:update");
    }

    private String generateRefreshCode(String refreshToken, String deviceCode){
        return "refresh:" + refreshToken + ":" +deviceCode;
    }

    private String generateUserCode(String userCode, String deviceCode){
        return "user:" + userCode + ":" +deviceCode;
    }
}

