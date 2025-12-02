package com.ll.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;


@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public void saveRefreshToken(String userCode, String deviceCode , String refreshToken) {
        redisTemplate.opsForValue().set(generateCode(userCode,deviceCode), refreshToken,Duration.ofDays(7));
    }

    public String getRefreshToken(String userCode, String deviceCode) {
        return redisTemplate.opsForValue().get(generateCode(userCode,deviceCode));
    }

    public void deleteRefreshToken(String userCode, String deviceCode) {
        if(!redisTemplate.hasKey(generateCode(userCode,deviceCode))) {
            return;
        }
        redisTemplate.delete(generateCode(userCode,deviceCode));
    }


    // 연속적인 Redis 갱신 보호

    public void setUpdateRedisLimit(){
        redisTemplate.opsForValue().set("refresh:update","1",Duration.ofMinutes(1));
    }

    public boolean checkUpdateRedisLimit(){
        return redisTemplate.hasKey("refresh:update");
    }

    private String generateCode(String userCode,String deviceCode){

        return "refresh:" + userCode + ":" +deviceCode;
    }
}

