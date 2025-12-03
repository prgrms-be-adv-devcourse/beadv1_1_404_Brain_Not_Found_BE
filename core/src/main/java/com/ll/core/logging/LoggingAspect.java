package com.ll.core.logging;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {
    private final HttpServletRequest request;

    /* ==========================
        POINTCUT
       ========================== */
    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void controllerPointcut() {}

    @Pointcut("@within(org.springframework.stereotype.Service)")
    public void servicePointcut() {}

    @Pointcut("@annotation(com.ll.core.logging.LogParams)")
    public void logParamsPointcut() {}

    @Around("logParamsPointcut()")
    public Object logParams(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        log.info("[PARAMS] {} | params={}",
                simpleSignature(pjp),
                Arrays.stream(args).map(this::toJson).toList()
        );
        return pjp.proceed();
    }

    /* ==========================
        REST CONTROLLER LOGGING
       ========================== */
    @Around("controllerPointcut()")
    public Object logRestController(ProceedingJoinPoint pjp) throws Throwable {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String sig = simpleSignature(pjp);
        try {
            log.info("[REQUEST] {} {} | {}", method, uri, sig);
            Object result = pjp.proceed();
            log.debug("[RESPONSE] {} {} | {}", method, uri, sig);
            return result;
        } catch (Exception e) {
            log.error("[ERROR] {} {} | {} | message={}", method, uri, sig, e.getMessage());
            throw e;
        }
    }

    /* ==========================
        SERVICE LOGGING
       ========================== */
    @Around("servicePointcut()")
    public Object logService(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        String sig = simpleSignature(pjp);
        try {
            Object result = pjp.proceed();
            long elapsed = System.currentTimeMillis() - start;
            logServiceTime(sig, elapsed);
            return result;
        } catch (Throwable ex) {
            log.error("[SERVICE-ERROR] {} | message={}", sig, ex.getMessage());
            throw ex;
        }
    }

    /* ==========================
        UTIL
       ========================== */
    private String toJson(Object obj) {
        try {
            String json = String.valueOf(obj);
            if (json.length() > 500) {
                return json.substring(0, 500) + "...(truncated)";
            }
            return json;
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }

    private void logServiceTime(String sig, long elapsed) {
        if (elapsed > 1000) {
            log.error("[SLOW][{}ms] {}", elapsed, sig);
        } else if (elapsed > 200) {
            log.warn("[WARN][{}ms] {}", elapsed, sig);
        } else {
            log.debug("[OK][{}ms] {}", elapsed, sig);
        }
    }

    private String simpleSignature(ProceedingJoinPoint pjp) {
        return pjp.getSignature().getDeclaringType().getSimpleName()
                + "." + pjp.getSignature().getName();
    }
}