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
        redisTemplate.opsForValue().set(generateCode(userCode,deviceCode), refreshToken,Duration.ofDays(7));
    }

    public String getRefreshToken(String userCode, String deviceCode) {
        String value = redisTemplate.opsForValue().get(generateCode(userCode,deviceCode));
        return Optional.ofNullable(value).orElseThrow(TokenNotFoundException::new);
    }

    public void deleteRefreshToken(String userCode, String deviceCode) {
        if(!redisTemplate.hasKey(generateCode(userCode,deviceCode))) {
            return;
        }
        redisTemplate.delete(generateCode(userCode,deviceCode));
    }

    private String generateCode(String userCode,String deviceCode){

        return "refresh" + userCode + ":" +deviceCode;
    }
}

