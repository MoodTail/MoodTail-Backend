package com.example.moodtail.domain.history.service;

import com.example.moodtail.global.common.exception.RestApiException;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

import static com.example.moodtail.global.common.exception.code.status.HistoryErrorStatus.INVALID_REQUEST;

public final class HistoryDatePolicy {

    private static final int MIN_SUPPORTED_YEAR = 1000;

    private HistoryDatePolicy() {
    }

    static LocalDate parse(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException | NullPointerException exception) {
            throw new RestApiException(INVALID_REQUEST);
        }
    }

    static void validateRecordDate(LocalDate date, LocalDate today) {
        if (date == null || date.getYear() < MIN_SUPPORTED_YEAR || date.isAfter(today)) {
            throw new RestApiException(INVALID_REQUEST);
        }
    }

    public static YearMonth parseYearMonth(int year, int month) {
        if (year < MIN_SUPPORTED_YEAR) {
            throw new RestApiException(INVALID_REQUEST);
        }
        try {
            return YearMonth.of(year, month);
        } catch (DateTimeException exception) {
            throw new RestApiException(INVALID_REQUEST);
        }
    }

    public static boolean isMinimumSupportedMonth(YearMonth yearMonth) {
        return yearMonth.getYear() == MIN_SUPPORTED_YEAR && yearMonth.getMonthValue() == 1;
    }
}
