package com.example.moodtail.domain.recommendation.service;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.cocktail.repository.CocktailRepository;
import com.example.moodtail.domain.moodtest.entity.MoodTestResult;
import com.example.moodtail.domain.moodtest.repository.MoodTestResultRepository;
import com.example.moodtail.domain.recommendation.calculator.TasteSimilarityCalculator;
import com.example.moodtail.domain.recommendation.dto.response.CompromiseProfileResponse;
import com.example.moodtail.domain.recommendation.dto.response.PairRecommendationResponse;
import com.example.moodtail.domain.recommendation.dto.response.RecommendedCocktailResponse;
import com.example.moodtail.domain.recommendation.model.RecommendationItemCommand;
import com.example.moodtail.domain.recommendation.model.TasteProfile;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.MoodTestErrorStatus;
import com.example.moodtail.global.common.exception.code.status.RecommendationErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PairRecommendationService {

    private static final int RECOMMENDATION_LIMIT = 4;

    private final MoodTestResultRepository moodTestResultRepository;
    private final CocktailRepository cocktailRepository;
    private final TasteSimilarityCalculator tasteSimilarityCalculator;
    private final PairRecommendationPersistenceService pairRecommendationPersistenceService;

    public PairRecommendationResponse recommendPair(
            Long userId,
            Long resultId,
            String resultShareToken,
            String partnerShareToken
    ) {
        MoodTestResult myResult = findResult(resultId, resultShareToken, userId);
        MoodTestResult partnerResult = findPartnerResult(partnerShareToken);

        TasteProfile compromise = myResult.toTasteProfile()
                .average(partnerResult.toTasteProfile());

        List<RecommendedCocktailResponse> recommendations = recommend(compromise);

        boolean saved = canSave(userId, resultId);
        if (saved) {
            pairRecommendationPersistenceService.saveCompromise(
                    myResult.getUser(),
                    myResult,
                    partnerResult,
                    toCommands(recommendations)
            );
        }

        return new PairRecommendationResponse(
                saved,
                CompromiseProfileResponse.from(compromise),
                recommendations
        );
    }

    private MoodTestResult findResult(Long resultId, String shareToken, Long userId) {
        if (resultId != null) {
            MoodTestResult result = moodTestResultRepository.findById(resultId)
                    .orElseThrow(() -> new RestApiException(MoodTestErrorStatus.MOOD_TEST_RESULT_NOT_FOUND));

            if (userId == null || !result.getUser().getId().equals(userId)) {
                throw new RestApiException(MoodTestErrorStatus.MOOD_TEST_RESULT_NOT_FOUND);
            }
            return result;
        }

        if (shareToken != null) {
            return moodTestResultRepository.findByShareToken(shareToken)
                    .orElseThrow(() -> new RestApiException(MoodTestErrorStatus.MOOD_TEST_RESULT_NOT_FOUND));
        }

        throw new RestApiException(RecommendationErrorStatus.RECOMMENDATION_INVALID_PARAMETER);
    }

    private MoodTestResult findPartnerResult(String partnerShareToken) {
        if (partnerShareToken == null) {
            throw new RestApiException(RecommendationErrorStatus.RECOMMENDATION_INVALID_PARAMETER);
        }

        return moodTestResultRepository.findByShareToken(partnerShareToken)
                .orElseThrow(() -> new RestApiException(MoodTestErrorStatus.MOOD_TEST_RESULT_NOT_FOUND));
    }

    private List<RecommendedCocktailResponse> recommend(TasteProfile compromise) {
        List<Cocktail> cocktails = cocktailRepository.findAll();

        if (cocktails.isEmpty()) {
            throw new RestApiException(RecommendationErrorStatus.RECOMMENDATION_UNAVAILABLE);
        }

        List<ScoredCocktail> scored = cocktails.stream()
                .map(cocktail -> new ScoredCocktail(
                        cocktail,
                        tasteSimilarityCalculator.calculateDistance(compromise, cocktail.toTasteProfile())
                ))
                .sorted(Comparator.comparingDouble(ScoredCocktail::distance))
                .limit(RECOMMENDATION_LIMIT)
                .toList();

        List<RecommendedCocktailResponse> responses = new ArrayList<>(scored.size());
        for (int i = 0; i < scored.size(); i++) {
            ScoredCocktail scoredCocktail = scored.get(i);
            int matchScore = tasteSimilarityCalculator.calculateMatchScore(scoredCocktail.distance());
            responses.add(RecommendedCocktailResponse.of(scoredCocktail.cocktail(), i + 1, matchScore));
        }
        return responses;
    }

    private boolean canSave(Long userId, Long resultId) {
        return userId != null && resultId != null;
    }

    private List<RecommendationItemCommand> toCommands(List<RecommendedCocktailResponse> recommendations) {
        return recommendations.stream()
                .map(r -> new RecommendationItemCommand(r.cocktailId(), r.matchScore()))
                .toList();
    }

    private record ScoredCocktail(Cocktail cocktail, double distance) {
    }
}