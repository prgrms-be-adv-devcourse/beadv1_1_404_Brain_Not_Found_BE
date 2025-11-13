package com.example.core.model.vo.kafka;

public record UserCreateEvent (
    Long userId,
    String userCode
) {
}
