package com.example.moodtail.domain.cocktail.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CocktailTrendResponse(
        List<PopularMoodType> popularMoodTypes,
        TasteProfile averageTasteProfile,
        DisplayTasteScores displayAverageTasteScores,
        List<PopularCocktail> popularCocktails,
        List<RankChangeCocktail> rankChangeCocktails
) {
    public CocktailTrendResponse {
        popularMoodTypes = List.copyOf(popularMoodTypes);
        popularCocktails = List.copyOf(popularCocktails);
        rankChangeCocktails = List.copyOf(rankChangeCocktails);
    }

    public record PopularMoodType(
            int ranking,
            Long moodTypeId,
            String typeCode,
            String name,
            long resultCount,
            int ratio
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

    // ranking/ratio/recordCount는 누적 전체 기간, rankChange만 이번주 월~일 vs 지난주 월~일
    public record PopularCocktail(
            int ranking,
            Long cocktailId,
            String nameKo,
            String nameEn,
            String shortDescription,
            int ratio,
            long recordCount,
            Integer rankChange
    ) {
    }

    public record RankChangeCocktail(
            Long cocktailId,
            String nameKo,
            String nameEn,
            int rankChange,
            ChangeDirection changeDirection
    ) {
    }

    public enum ChangeDirection {
        UP,
        DOWN
    }
}
