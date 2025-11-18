package com.ll.user.model.vo.response;

import com.ll.user.model.entity.User;
import com.ll.user.model.enums.AccountStatus;
import com.ll.user.model.enums.Grade;
import com.ll.user.model.enums.Role;
import com.ll.user.model.enums.SocialProvider;

import java.time.LocalDateTime;
import java.util.Map;

public record UserResponse (
        Long id,
        String code,
        String socialId,
        SocialProvider socialProvider,
        String email,
        String name,
        Role role,
        String profileImageUrl,
        Long mannerScore,
        Grade grade,
        AccountStatus accountStatus,
        String accountBank,
        String accountNumber,
        LocalDateTime createAt,
        LocalDateTime updatedAt
){
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getCode(),
                user.getSocialId(),
                user.getSocialProvider(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getProfileImageUrl(),
                user.getMannerScore(),
                user.getGrade(),
                user.getAccountStatus(),
                user.getAccountBank(),
                user.getAccountNumber(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public static UserResponse from(Map<String, Object> userData) {
        return new UserResponse(
                getLong(userData, "id"),
                getString(userData, "code"),
                getString(userData, "socialId"),
                getEnum(userData, "socialProvider", SocialProvider.class),
                getString(userData, "email"),
                getString(userData, "name"),
                getEnum(userData, "role", Role.class),
                getString(userData, "profileImageUrl"),
                getLong(userData, "mannerScore"),
                getEnum(userData, "grade", Grade.class),
                getEnum(userData, "accountStatus", AccountStatus.class),
                getString(userData, "accountBank"),
                getString(userData, "accountNumber"),
                getLocalDateTime(userData, "createAt"),
                getLocalDateTime(userData, "updatedAt")
        );
    }

    private static Long getLong(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        return Long.valueOf(value.toString());
    }

    private static String getString(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value == null ? null : value.toString();
    }

    private static <T extends Enum<T>> T getEnum(Map<String, Object> data, String key, Class<T> enumClass) {
        Object value = data.get(key);
        if (value == null) return null;
        if (enumClass.isInstance(value)) return enumClass.cast(value);
        return Enum.valueOf(enumClass, value.toString());
    }

    private static LocalDateTime getLocalDateTime(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) return null;
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        return LocalDateTime.parse(value.toString());
    }
}
