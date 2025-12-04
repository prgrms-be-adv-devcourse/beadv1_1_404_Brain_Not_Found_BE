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
    private final Long TTL = 15L;          // 데이터 유지기간 (일)
    private final Long dataRange = 30L;    // 저장 최대 개수

    public void saveSearchData(String userCode, String keyword) {
        String key = generateSearchCode(userCode);
        try {
            redisTemplate.opsForList().leftPush(key, keyword);
            redisTemplate.opsForList().trim(key, 0, dataRange - 1); // 최대 개수 유지
            redisTemplate.expire(key, Duration.ofDays(TTL));
            log.info("Redis 검색 기록 저장 성공 :{}",keyword);
        } catch (Exception e) {
            log.warn("Redis 검색 기록 저장 실패: {}", e.getMessage());
        }
    }

    public void saveViewData(String userCode, String productCode) {
        String key = generateViewCode(userCode);
        try {
            redisTemplate.opsForList().leftPush(key, productCode);
            redisTemplate.opsForList().trim(key, 0, dataRange - 1); // 최대 개수 유지
            redisTemplate.expire(key, Duration.ofDays(TTL));
            log.info("Redis 조회 기록 저장 성공 :{}", productCode);
        } catch (Exception e) {
            log.warn("Redis 조회 기록 저장 실패: {}", e.getMessage());
        }
    }

    public List<String> getSearchData(String userCode){
        return redisTemplate.opsForList().range(generateSearchCode(userCode), 0, dataRange - 1);
    }

    public List<String> getViewData(String userCode){
        return redisTemplate.opsForList().range(generateViewCode(userCode), 0, dataRange - 1);
    }

    private String generateSearchCode(String userCode){
        return "search:" + userCode;
    }

    private String generateViewCode(String userCode){
        return "view:" + userCode;
    }
}
