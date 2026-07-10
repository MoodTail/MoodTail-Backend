package com.example.moodtail.domain.cocktail.dto.response;

import com.example.moodtail.domain.moodtest.entity.MoodType;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record MoodTypeResponse(
        MoodTypeDto moodType,
        TypeFiguresDto typeFigures,
        MatchTypeDto bestMatchType,
        MatchTypeDto worstMatchType,
        List<CocktailSummaryDto> cocktails
) {
    @Builder
    public record MoodTypeDto(
            Long typeId,
            String typeCode,
            String name,
            String description,
            String imageUrl,
            Integer typePercent
    ) {}

    @Builder
    public record TypeFiguresDto(
            int alcoholIntensity,
            int sweetness,
            int sourness,
            int bitterness,
            int refreshing
    ) {}

    @Builder
    public record MatchTypeDto(
            Long typeId,
            String name
    ) {}

    @Builder
    public record CocktailSummaryDto(
            Long cocktailId,
            String name,
            String shortDescription,
            String imageUrl
    ) {}
}