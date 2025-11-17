package com.ll.core.logging.kafka;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class KafkaListenerLoggingAspect {

    @Around("@annotation(org.springframework.kafka.annotation.KafkaListener)")
    public Object logKafkaListener(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        log.info("[KafkaListener called: {} args={}", pjp.getSignature().getName(), Arrays.toString(args));

        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            log.info("KafkaListener succeeded: {} took={}ms", pjp.getSignature().getName(), System.currentTimeMillis() - start);
            return result;
        } catch (Exception ex) {
            log.error("KafkaListener failed: {} error={}", pjp.getSignature().getName(), ex.getMessage(), ex);
            throw ex;
        }
    }

}

