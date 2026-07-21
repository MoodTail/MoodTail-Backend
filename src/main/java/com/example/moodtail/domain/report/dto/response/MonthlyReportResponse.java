package com.example.moodtail.domain.report.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record MonthlyReportResponse(
        int year,
        int month,
        MoodType monthlyMoodType,
        List<RankedMoodType> topMoodTypes,
        TasteProfile averageTasteProfile,
        DisplayTasteScores displayAverageTasteScores,
        TasteProfile previousMonthTasteProfile,
        DisplayTasteScores previousMonthDisplayTasteScores,
        List<FrequentCocktail> frequentCocktails,
        Activity activity
) {
    public MonthlyReportResponse {
        topMoodTypes = List.copyOf(topMoodTypes);
        frequentCocktails = List.copyOf(frequentCocktails);
    }

    public record MoodType(
            Long moodTypeId,
            String typeCode,
            String name,
            String shortDescription,
            String characterImageUrl
    ) {
    }

    public record RankedMoodType(
            Long moodTypeId,
            String typeCode,
            String name,
            String characterImageUrl,
            long count,
            int ranking
    ) {
    }

    public record TasteProfile(
            BigDecimal alcoholIntensity,
            BigDecimal sweetness,
            BigDecimal sourness,
            BigDecimal refreshing,
            BigDecimal bitterness
    ) {
    }

    public record DisplayTasteScores(
            int alcoholIntensity,
            int sweetness,
            int sourness,
            int refreshing,
            int bitterness
    ) {
    }

    public record FrequentCocktail(
            Long cocktailId,
            String nameKo,
            String nameEn,
            String shortDescription,
            String imageUrl,
            long count,
            int ranking
    ) {
    }

    public record Activity(long testCount, long drinkingRecordCount) {
    }
}
