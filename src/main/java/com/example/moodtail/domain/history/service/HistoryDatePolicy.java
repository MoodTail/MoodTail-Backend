package com.example.moodtail.domain.history.service;

import com.example.moodtail.global.common.exception.RestApiException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static com.example.moodtail.global.common.exception.code.status.HistoryErrorStatus.INVALID_REQUEST;

final class HistoryDatePolicy {

    private static final int MYSQL_MIN_YEAR = 1000;

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
        if (date == null || date.getYear() < MYSQL_MIN_YEAR || date.isAfter(today)) {
            throw new RestApiException(INVALID_REQUEST);
        }
    }
}
