package com.ll.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public void saveRefreshToken(String userCode, String deviceCode , String refreshToken) {
        redisTemplate.opsForHash().put(userCode, deviceCode, refreshToken);
    }

    public String getRefreshToken(String userCode, String deviceCode) {
        return redisTemplate.opsForHash().get(userCode,deviceCode).toString();
    }

    public void deleteRefreshToken(String userCode, String deviceCode) {
        redisTemplate.opsForHash().delete(userCode, deviceCode);
    }
}

