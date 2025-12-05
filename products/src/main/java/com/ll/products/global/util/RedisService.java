package com.ll.products.global.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    private final Long TTL = 15L;       // 15일 유지
    private final Long dataRange = 30L; // 최근 30개 유지

    public void pushSearchData(String userCode, String keyword) {
        String key = generateSearchKey(userCode);

        redisTemplate.opsForList().leftPush(key, keyword);
        redisTemplate.opsForList().trim(key, 0, dataRange - 1);
        redisTemplate.expire(key, Duration.ofDays(TTL));
    }

    public List<String> getSearchData(String userCode) {
        return redisTemplate.opsForList().range(generateSearchKey(userCode), 0, dataRange - 1);
    }

    private String generateSearchKey(String userCode) {
        return "search:" + userCode;
    }

    public void pushViewData(String userCode, String productCode) {
        String key = generateViewKey(userCode);

        redisTemplate.opsForList().leftPush(key, productCode);
        redisTemplate.opsForList().trim(key, 0, dataRange - 1);
        redisTemplate.expire(key, Duration.ofDays(TTL));
    }

    public List<String> getViewData(String userCode) {
        return redisTemplate.opsForList().range(generateViewKey(userCode), 0, dataRange - 1);
    }

    private String generateViewKey(String userCode) {
        return "view:" + userCode;
    }
}
