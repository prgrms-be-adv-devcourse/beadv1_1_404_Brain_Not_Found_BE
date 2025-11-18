package com.ll.core.model.vo.kafka;

import com.ll.core.model.vo.kafka.enums.UserCreateEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserCreateEvent (
        @NotNull(message = "userId 는 필수입니다.")
        Long userId,
        @NotBlank(message = "userCode 는 공백이거나 null일 수 없습니다.")
        String userCode,
        @NotNull(message = "eventType 는 필수입니다.")
        UserCreateEventType eventType
) {
    public static UserCreateEvent depositTriggerFrom(Long userId, String userCode) {
        return new UserCreateEvent(userId, userCode, UserCreateEventType.DEPOSIT_CREATE);
    }

    public static UserCreateEvent cartTriggerFrom(Long userId, String userCode) {
        return new UserCreateEvent(userId, userCode, UserCreateEventType.CART_CREATE);
    }
}
