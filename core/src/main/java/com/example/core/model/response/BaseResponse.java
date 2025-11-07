package com.example.core.model.response;

import com.example.core.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {

    private int status;

    @Builder.Default
    private String message = "success";

    private ErrorCode errorCode;
    private T data;

    // 200 OK — 일반 성공
    public static <T> ResponseEntity<BaseResponse<T>> ok(T data) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.<T>builder()
                        .status(HttpStatus.OK.value())
                        .data(data)
                        .build());
    }

    // 201 Created — 리소스 생성 성공
    public static <T> ResponseEntity<BaseResponse<T>> created(T data) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.<T>builder()
                        .status(HttpStatus.CREATED.value())
                        .data(data)
                        .build());
    }

    // Error Response — 에러 공통 처리
    public static <T> ResponseEntity<BaseResponse<T>> error(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(BaseResponse.<T>builder()
                        .status(errorCode.getStatus().value())
                        .message(errorCode.getMessage())
                        .errorCode(errorCode)
                        .build());
    }

    public static <T> ResponseEntity<BaseResponse<T>> error(ErrorCode errorCode, String message) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(BaseResponse.<T>builder()
                        .status(errorCode.getStatus().value())
                        .message(message)
                        .errorCode(errorCode)
                        .build());
    }
}
