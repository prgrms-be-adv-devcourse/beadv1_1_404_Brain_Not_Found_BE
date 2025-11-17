package com.ll.core.model.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 기본 ERROR CODES
    // 400 — 잘못된 요청
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다."),

    // 401 — 인증 실패
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증 정보가 유효하지 않습니다."),

    // 403 — 권한 부족
    FORBIDDEN(HttpStatus.FORBIDDEN, "해당 리소스에 접근 권한이 없습니다."),

    // 404 — 리소스 없음
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),

    // 409 — 충돌 발생
    CONFLICT(HttpStatus.CONFLICT, "데이터 충돌이 발생했습니다."),

    // 422 — 처리 불가능한 엔티티
    UNPROCESSABLE_ENTITY(HttpStatus.UNPROCESSABLE_ENTITY, "도메인 규칙에의해 요청하신 입력에 대한 처리가 불가합니다."),

    // 500 — 내부 서버 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // 502 — 게이트웨이 오류
    BAD_GATEWAY(HttpStatus.BAD_GATEWAY, "게이트웨이 통신 중 오류가 발생했습니다."),

    // 503 — 서비스 이용 불가
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "현재 서비스가 일시적으로 중단되었습니다."),

    // 504 — 게이트웨이 타임아웃
    GATEWAY_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "게이트웨이 응답 시간이 초과되었습니다."),

    // CUSTOM ERROR CODES
    DEPOSIT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 입금 계좌입니다."),
    DEPOSIT_ALREADY_CLOSED(HttpStatus.CONFLICT, "이미 비활성 상태인 입금 계좌입니다."),
    BALANCE_NOT_EMPTY(HttpStatus.UNPROCESSABLE_ENTITY, "잔액이 남아있는 입금 계좌는 삭제할 수 없습니다."),
    SETTLEMENT_INVALID_STATE_TRANSITION(HttpStatus.CONFLICT, "정산의 상태 전이가 유효하지 않습니다."),
    SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 정산을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
