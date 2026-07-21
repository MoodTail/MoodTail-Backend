package com.example.moodtail.domain.history.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record HistoryTestResultDetailResponse(
        Long resultId,
        LocalDate resultDate,
        MoodType moodType,
        TasteProfile tasteProfile,
        List<RecommendedCocktail> recommendedCocktails
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
            String characterImageUrl
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
            String cocktailImageUrl,
            int ranking,
            int matchScore
    ) {
    }
}
