package com.ll.core.model.vo.kafka;

import com.ll.core.model.vo.kafka.enums.UserCreateEventType;

public record UserCreateEvent (
        Long userId,
        String userCode,
        UserCreateEventType eventType,
        String referenceCode // 중복 발행 방지용
) {
}
