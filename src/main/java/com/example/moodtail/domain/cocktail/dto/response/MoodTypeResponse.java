package com.example.moodtail.domain.cocktail.dto.response;

import com.example.moodtail.domain.moodtest.entity.MoodType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record MoodTypeResponse(
        Long moodTypeId,
        String typeCode,
        String name,
        String shortDescription,
        String description,
        String catchphrase,
        String characterImageUrl,
        boolean unlocked,
        boolean representative,
        boolean canSetRepresentative,
        int typePercent,
        int collectionRate,
        TypeFiguresDto typeFigures,
        CompatibilitiesDto compatibilities,
        List<CocktailSummaryDto> cocktails,
        int totalCocktailCount,
        int unlockedCocktailCount
) {
    @Builder
    public record TypeFiguresDto(
            int alcoholIntensity,
            int sweetness,
            int sourness,
            int bitterness,
            int refreshing
    ) {}

    @Builder
    public record CompatibilitiesDto(
            CompatibilityDto best,
            CompatibilityDto worst
    ) {}

    @Builder
    public record CompatibilityDto(
            Long moodTypeId,
            String typeCode,
            String name,
            String characterImageUrl
    ) {}

    @Builder
    public record CocktailSummaryDto(
            Long cocktailId,
            String nameKo,
            String nameEn,
            String shortDescription,
            String imageUrl,
            boolean unlocked
    ) {}
}