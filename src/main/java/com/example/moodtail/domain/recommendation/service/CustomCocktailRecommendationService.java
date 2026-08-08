package com.example.moodtail.domain.recommendation.service;

import com.example.moodtail.domain.cocktail.dto.request.CustomCocktailRecommendationRequest;
import com.example.moodtail.domain.cocktail.dto.response.CustomCocktailRecommendationResponse;
import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.cocktail.repository.CocktailRepository;
import com.example.moodtail.domain.recommendation.calculator.TasteProfileCalculator;
import com.example.moodtail.domain.recommendation.calculator.TasteSimilarityCalculator;
import com.example.moodtail.domain.recommendation.model.TasteProfile;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.RecommendationErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class CustomCocktailRecommendationService {

    private final CocktailRepository cocktailRepository;
    private final TasteProfileCalculator tasteProfileCalculator;
    private final TasteSimilarityCalculator tasteSimilarityCalculator;

    @Transactional(readOnly = true)
    public CustomCocktailRecommendationResponse recommend(
            CustomCocktailRecommendationRequest request
    ) {
        TasteProfile userTasteProfile =
                tasteProfileCalculator.calculateFromDisplayScores(
                        request.alcoholIntensity(),
                        request.sweetness(),
                        request.sourness(),
                        request.refreshing(),
                        request.bitterness()
                );

        RecommendationCandidate recommendation =
                cocktailRepository.findAllByOrderByIdAsc().stream()
                        .map(cocktail ->
                                calculateCandidate(
                                        userTasteProfile,
                                        cocktail
                                )
                        )
                        .min(Comparator
                                .comparingDouble(
                                        RecommendationCandidate::distance
                                )
                                .thenComparing(
                                        candidate ->
                                                candidate.cocktail().getId()
                                ))
                        .orElseThrow(() ->
                                new RestApiException(
                                        RecommendationErrorStatus.RECOMMENDATION_UNAVAILABLE
                                ));

        return CustomCocktailRecommendationResponse.of(
                recommendation.cocktail(),
                recommendation.matchRate(),
                recommendation.cocktail().getShortDescription(),
                request,
                tasteProfileCalculator
        );
    }

    private RecommendationCandidate calculateCandidate(
            TasteProfile userTasteProfile,
            Cocktail cocktail
    ) {
        double distance =
                tasteSimilarityCalculator.calculateDistance(
                        userTasteProfile,
                        cocktail.toTasteProfile()
                );

        int matchRate =
                tasteSimilarityCalculator.calculateMatchScore(
                        distance
                );

        return new RecommendationCandidate(
                cocktail,
                distance,
                matchRate
        );
    }

    private record RecommendationCandidate(
            Cocktail cocktail,
            double distance,
            int matchRate
    ) {
    }
}
