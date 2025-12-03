package com.ll.core.model.exception;

import com.ll.core.model.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String LOG_PREFIX = "[GlobalExceptionHandler] Unhandled exception occurred: {}";

    // 커스텀 예외 (BaseException)
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseResponse<Void>> handleBaseException(BaseException e) {
        return BaseResponse.error(e.getErrorCode(), e.getMessage());
    }

    // 검증 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        log.error(LOG_PREFIX, e.getMessage(), e);
        List<String> messages = e.getBindingResult().getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
        return BaseResponse.error(ErrorCode.BAD_REQUEST, String.join(", ", messages));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<BaseResponse<Void>> handleHandlerMethodValidation(HandlerMethodValidationException e) {
        log.error(LOG_PREFIX, e.getMessage(), e);
        List<String> messages = e.getAllErrors()
                .stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .toList();
        return BaseResponse.error(ErrorCode.BAD_REQUEST, String.join(", ", messages));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseResponse<Void>> handleMissingRequestParam(MissingServletRequestParameterException e) {
        log.error(LOG_PREFIX, e.getMessage(), e);
        return BaseResponse.error(ErrorCode.BAD_REQUEST, String.format("필수 파라미터 '%s'가 누락되었습니다.", e.getParameterName()));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<BaseResponse<Void>> handleMissingRequestHeader(MissingRequestHeaderException e) {
        log.error(LOG_PREFIX, e.getMessage(), e);
        return BaseResponse.error(ErrorCode.BAD_REQUEST, String.format("필수 헤더 '%s'가 누락되었습니다.", e.getHeaderName()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFound(NoResourceFoundException e) {
        // favicon.ico 같은 리소스 요청은 굳이 로그 남기지 않음
        return ResponseEntity.notFound().build();
    }

    // 그 외 모든 예외 (예상치 못한 에러)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleGenericException(Exception e) {
        log.warn(LOG_PREFIX, e.getMessage(), e);
        return BaseResponse.error(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
