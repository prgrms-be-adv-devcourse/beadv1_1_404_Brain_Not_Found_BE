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
    private final Long TTL = 15L;
    private final Long dataRange = 30L;

    public void saveSearchData(String userCode, String keyword) {
        String key = generateSearchCode(userCode);
        redisTemplate.opsForList().leftPush(key,keyword);
        redisTemplate.expire(key, Duration.ofDays(TTL));
    }

    public void saveViewData(String userCode, String productCode) {
        String key = generateViewCode(userCode);
        redisTemplate.opsForList().leftPush(key,productCode);
        redisTemplate.expire(key, Duration.ofDays(TTL));
    }

    public List<String> getSearchData(String userCode){
        return redisTemplate.opsForList().range(generateSearchCode(userCode),0,dataRange);
    }

    public List<String> getViewData(String userCode){
        return redisTemplate.opsForList().range(generateViewCode(userCode),0,dataRange);
    }

    private String generateSearchCode(String userCode){
        return "search:" + userCode;
    }

    private String generateViewCode(String userCode){
        return "view:" + userCode;
    }
}

