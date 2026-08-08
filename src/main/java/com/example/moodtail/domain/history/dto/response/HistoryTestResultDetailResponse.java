package com.example.moodtail.domain.history.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record HistoryTestResultDetailResponse(
        Long resultId,
        LocalDate resultDate,
        MoodType moodType,
        TasteProfile tasteProfile,
        DisplayTasteScores displayTasteScores,
        List<RecommendedCocktail> recommendedCocktails,
        Compatibilities compatibilities
) {
    public HistoryTestResultDetailResponse {
        recommendedCocktails = List.copyOf(recommendedCocktails);
    }

    public record MoodType(
            Long moodTypeId,
            String typeCode,
            String name,
            String shortDescription,
            String description,
            String characterQuote,
            String characterImageUrl,
            DisplayTasteScores displayTasteScores
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

    public record RecommendedCocktail(
            Long cocktailId,
            String cocktailName,
            String shortDescription,
            String cocktailImageUrl,
            int ranking,
            int matchScore
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

    public record Compatibilities(
            CompatibleMoodType best,
            CompatibleMoodType worst
    ) {
    }

    public record CompatibleMoodType(
            Long moodTypeId,
            String typeCode,
            String name
    ) {
    }
}
