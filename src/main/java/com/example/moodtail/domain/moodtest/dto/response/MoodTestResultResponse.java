package com.example.moodtail.domain.moodtest.dto.response;

import com.example.moodtail.domain.moodtest.entity.Cocktail;
import com.example.moodtail.domain.moodtest.entity.MoodType;
import com.example.moodtail.domain.recommendation.model.TasteProfile;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record MoodTestResultResponse(
        Long resultId,
        boolean saved,
        MoodTypeDto moodType,
        TasteProfileDto tasteProfile,
        DisplayTasteScoresDto displayTasteScores,
        List<RecommendationDto> recommendations,
        CompatibilityDto compatibilities
) {

    public static MoodTypeDto moodTypeDto(MoodType moodType) {
        return MoodTypeDto.builder()
                .moodTypeId(moodType.getId())
                .typeCode(moodType.getCode())
                .name(moodType.getName())
                .shortDescription(moodType.getShortDescription())
                .characterQuote(moodType.getCharacterQuote())
                .characterImageUrl(moodType.getCharacterImage() == null
                        ? null
                        : moodType.getCharacterImage().getImageUrl())
                .displayTasteScores(displayTasteScoresDto(moodType.toTasteProfile()))
                .build();
    }

    public static TasteProfileDto tasteProfileDto(TasteProfile tasteProfile) {
        return TasteProfileDto.builder()
                .alcoholIntensity(tasteProfile.alcoholIntensity())
                .sweetness(tasteProfile.sweetness())
                .sourness(tasteProfile.sourness())
                .refreshing(tasteProfile.refreshing())
                .bitterness(tasteProfile.bitterness())
                .build();
    }

    public static DisplayTasteScoresDto displayTasteScoresDto(TasteProfile tasteProfile) {
        return DisplayTasteScoresDto.builder()
                .alcoholIntensity(toDisplayScore(tasteProfile.alcoholIntensity()))
                .sweetness(toDisplayScore(tasteProfile.sweetness()))
                .sourness(toDisplayScore(tasteProfile.sourness()))
                .refreshing(toDisplayScore(tasteProfile.refreshing()))
                .bitterness(toDisplayScore(tasteProfile.bitterness()))
                .build();
    }

    public static RecommendationDto recommendationDto(int ranking, Cocktail cocktail, int matchScore) {
        return RecommendationDto.builder()
                .ranking(ranking)
                .cocktailId(cocktail.getId())
                .nameKo(cocktail.getNameKo())
                .nameEn(cocktail.getNameEn())
                .shortDescription(cocktail.getShortDescription())
                .imageUrl(cocktail.getImage() == null ? null : cocktail.getImage().getImageUrl())
                .matchScore(matchScore)
                .build();
    }

    private static int toDisplayScore(BigDecimal score) {
        return (int) Math.round((score.doubleValue() - 1.0) / 4.0 * 100.0);
    }

    @Builder
    public record MoodTypeDto(
            Long moodTypeId,
            String typeCode,
            String name,
            String shortDescription,
            String characterQuote,
            String characterImageUrl,
            DisplayTasteScoresDto displayTasteScores
    ) {
    }

    @Builder
    public record TasteProfileDto(
            BigDecimal alcoholIntensity,
            BigDecimal sweetness,
            BigDecimal sourness,
            BigDecimal refreshing,
            BigDecimal bitterness
    ) {
    }

    @Builder
    public record DisplayTasteScoresDto(
            int alcoholIntensity,
            int sweetness,
            int sourness,
            int refreshing,
            int bitterness
    ) {
    }

    @Builder
    public record RecommendationDto(
            int ranking,
            Long cocktailId,
            String nameKo,
            String nameEn,
            String shortDescription,
            String imageUrl,
            int matchScore
    ) {
    }

    @Builder
    public record CompatibilityDto(
            MoodTypeDto best,
            MoodTypeDto worst
    ) {
    }
}
