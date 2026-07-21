package com.example.moodtail.domain.recommendation.service;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.recommendation.calculator.CosineSimilarityCalculator;
import com.example.moodtail.domain.recommendation.model.CocktailRecommendationResult;
import com.example.moodtail.domain.recommendation.model.WeatherTasteVector;
import com.example.moodtail.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

import static com.example.moodtail.global.common.exception.code.status.RecommendationErrorStatus.RECOMMENDATION_UNAVAILABLE;

@Component
@RequiredArgsConstructor
public class DailyCocktailRecommender {

    private final CosineSimilarityCalculator cosineSimilarityCalculator;

    public CocktailRecommendationResult recommend(
            WeatherTasteVector todayVector,
            List<Cocktail> cocktails
    ) {
        if (cocktails.isEmpty()) {
            throw new RestApiException(RECOMMENDATION_UNAVAILABLE);
        }

        return cocktails.stream()
                .map(cocktail -> {
                    WeatherTasteVector cocktailVector =
                            WeatherTasteVector.from(cocktail.toTasteProfile());

                    double similarity =
                            cosineSimilarityCalculator.calculate(
                                    todayVector,
                                    cocktailVector
                            );

                    return new CocktailRecommendationResult(
                            cocktail,
                            similarity
                    );
                })
                .max(Comparator
                        .comparingDouble(
                                CocktailRecommendationResult::similarity
                        )
                        .thenComparing(
                                result -> result.cocktail().getId()
                        ))
                .orElseThrow(() ->
                        new RestApiException(RECOMMENDATION_UNAVAILABLE));
    }
}
