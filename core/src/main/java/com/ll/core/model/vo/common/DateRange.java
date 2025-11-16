package com.ll.core.model.vo.common;

import com.ll.core.model.exception.InvalidDateRangeException;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DateRange(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate fromDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate toDate
) {
    public void validate() {
        if (fromDate != null && toDate != null && toDate.isBefore(fromDate)) {
            throw new InvalidDateRangeException("조회 종료일은 시작일보다 빠를 수 없습니다.");
        }
    }

    public LocalDateTime getStartDateTime() {
        return fromDate != null ? fromDate.atStartOfDay() : LocalDate.of(1970, 1, 1).atStartOfDay();
    }

    public LocalDateTime getEndDateTime() {
        return toDate != null ? toDate.plusDays(1).atStartOfDay() : LocalDate.now().plusDays(1).atStartOfDay();
    }
}
