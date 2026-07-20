package com.example.moodtail.domain.cocktail.dto.response;

import com.example.moodtail.domain.cocktail.dto.request.CustomCocktailRecommendationRequest;
import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.recommendation.calculator.TasteProfileCalculator;
import lombok.Builder;

@Builder
public record CustomCocktailRecommendationResponse (
        Long cocktailId,
        String name,
        String description,
        String imageUrl,
        int matchRate,
        FiguresDto userFigures,
        FiguresDto cocktailFigures
){
    public static CustomCocktailRecommendationResponse of(
            Cocktail cocktail,
            int matchRate,
            String description,
            CustomCocktailRecommendationRequest request,
            TasteProfileCalculator tasteProfileCalculator
    ) {
        return CustomCocktailRecommendationResponse.builder()
                .cocktailId(cocktail.getId())
                .name(cocktail.getNameKo())
                .description(description)
                .imageUrl(
                        cocktail.getImage() == null
                                ? null
                                : cocktail.getImage().getImageUrl()
                )
                .matchRate(matchRate)
                .userFigures(
                        FiguresDto.from(request)
                )
                .cocktailFigures(
                        FiguresDto.from(
                                cocktail,
                                tasteProfileCalculator
                        )
                )
                .build();
    }

    @Builder
    public record FiguresDto(
            int alcoholIntensity,
            int sweetness,
            int sourness,
            int refreshing,
            int bitterness
    ) {

        public static FiguresDto from(
                CustomCocktailRecommendationRequest request
        ) {
            return FiguresDto.builder()
                    .alcoholIntensity(request.alcoholIntensity())
                    .sweetness(request.sweetness())
                    .sourness(request.sourness())
                    .refreshing(request.refreshing())
                    .bitterness(request.bitterness())
                    .build();
        }

        public static FiguresDto from(
                Cocktail cocktail,
                TasteProfileCalculator tasteProfileCalculator
        ) {
            return FiguresDto.builder()
                    .alcoholIntensity(
                            tasteProfileCalculator.calculateDisplayScore(
                                    cocktail.getAlcoholIntensity()
                            )
                    )
                    .sweetness(
                            tasteProfileCalculator.calculateDisplayScore(
                                    cocktail.getSweetness()
                            )
                    )
                    .sourness(
                            tasteProfileCalculator.calculateDisplayScore(
                                    cocktail.getSourness()
                            )
                    )
                    .refreshing(
                            tasteProfileCalculator.calculateDisplayScore(
                                    cocktail.getRefreshing()
                            )
                    )
                    .bitterness(
                            tasteProfileCalculator.calculateDisplayScore(
                                    cocktail.getBitterness()
                            )
                    )
                    .build();
        }
    }
}
