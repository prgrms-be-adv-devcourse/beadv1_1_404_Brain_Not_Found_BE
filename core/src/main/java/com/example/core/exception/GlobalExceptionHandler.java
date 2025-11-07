package com.example.core.exception;

import com.example.core.model.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String LOG_PREFIX = "[GlobalExceptionHandler] Unhandled exception occurred: {}";

    // 커스텀 예외 (BaseException)
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseResponse<Void>> handleBaseException(BaseException e) {
        log.error(LOG_PREFIX, e.getMessage(), e);
        return BaseResponse.error(e.getErrorCode());
    }

    // 유효성 검증 예외 (ex: @Valid 실패)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        log.error(LOG_PREFIX, e.getMessage(), e);
        List<String> messages = e.getBindingResult().getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
        return BaseResponse.error(ErrorCode.BAD_REQUEST, String.join(", ", messages));
    }

    // 그 외 모든 예외 (예상치 못한 에러)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleGenericException(Exception e) {
        log.error(LOG_PREFIX, e.getMessage(), e);
        return BaseResponse.error(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
