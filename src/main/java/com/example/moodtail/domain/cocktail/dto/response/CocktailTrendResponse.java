package com.example.moodtail.domain.cocktail.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.List;

public record CocktailTrendResponse(
        String period,
        List<PopularMoodType> popularMoodTypes,
        TasteProfile averageTasteProfile,
        DisplayTasteScores displayAverageTasteScores,
        List<PopularCocktail> popularCocktails,
        List<RankChangeCocktail> rankChangeCocktails,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        List<SameTypePopularCocktail> sameTypePopularCocktails
) {
    public CocktailTrendResponse {
        popularMoodTypes = List.copyOf(popularMoodTypes);
        popularCocktails = List.copyOf(popularCocktails);
        rankChangeCocktails = List.copyOf(rankChangeCocktails);
        sameTypePopularCocktails = sameTypePopularCocktails == null ? null : List.copyOf(sameTypePopularCocktails);
    }

    public record PopularMoodType(
            int ranking,
            Long moodTypeId,
            String typeCode,
            String name,
            long resultCount,
            int ratio,
            Integer rankChange
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

    public record SameTypePopularCocktail(
            int ranking,
            Long cocktailId,
            String nameKo,
            long recordCount
    ) {
    }

    public enum ChangeDirection {
        UP,
        DOWN
    }
}
