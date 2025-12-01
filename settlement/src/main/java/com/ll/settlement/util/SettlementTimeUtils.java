package com.ll.settlement.util;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class SettlementTimeUtils {

    private static final DateTimeFormatter yearMonthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

    public static String getPreviousMonthStr() {
        return LocalDateTime.now().minusMonths(1).format(yearMonthFormatter);
    }

    public static YearMonth getYearMonth(String dateStr) {
        return YearMonth.parse(dateStr);
    }

    public static LocalDateTime getStartDay(String dateStr) {
        return getYearMonth(dateStr).atDay(1).atStartOfDay();
    }

    public static LocalDateTime getEndDay(String dateStr) {
        return getYearMonth(dateStr).atEndOfMonth().atTime(LocalTime.MAX);
    }

}
