package com.example.moodtail.domain.history.dto.response;

import java.time.LocalDate;
import java.util.List;

public record HistoryCalendarResponse(
        int year,
        int month,
        long testResultCount,
        long drinkingRecordCount,
        int reportRequiredTestCount,
        boolean reportAvailable,
        List<MonthlyTestResult> testResults,
        List<Day> days
) {
    public HistoryCalendarResponse {
        testResults = List.copyOf(testResults);
        days = List.copyOf(days);
    }

    public record MonthlyTestResult(
            Long resultId,
            LocalDate resultDate,
            MoodType moodType,
            List<DrinkingRecordSummary> drinkingRecords
    ) {
        public MonthlyTestResult {
            drinkingRecords = List.copyOf(drinkingRecords);
        }
    }

    public record DrinkingRecordSummary(
            Long recordId,
            Long cocktailId,
            String cocktailName
    ) {
    }

    public record Day(
            LocalDate date,
            boolean hasTestResult,
            boolean hasDrinkingRecord,
            long photoCount,
            MoodType moodType
    ) {
    }

    public record MoodType(
            Long moodTypeId,
            String typeCode,
            String name,
            String characterImageUrl
    ) {
    }
}
