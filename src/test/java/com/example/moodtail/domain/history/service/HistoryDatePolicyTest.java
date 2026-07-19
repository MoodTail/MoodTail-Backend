package com.example.moodtail.domain.history.service;

import com.example.moodtail.global.common.exception.RestApiException;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HistoryDatePolicyTest {

    @Test
    void acceptsTheMinimumSupportedYear() {
        YearMonth yearMonth = HistoryDatePolicy.parseYearMonth(1000, 1);

        assertThat(yearMonth).isEqualTo(YearMonth.of(1000, 1));
        assertThat(HistoryDatePolicy.isMinimumSupportedMonth(yearMonth)).isTrue();
    }

    @Test
    void rejectsAYearBelowTheSupportedRange() {
        assertThatThrownBy(() -> HistoryDatePolicy.parseYearMonth(999, 12))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("HISTORY_400"));
    }
}
