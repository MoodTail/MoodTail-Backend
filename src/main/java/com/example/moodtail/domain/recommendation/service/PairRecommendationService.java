package com.example.moodtail.domain.recommendation.service;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.cocktail.repository.CocktailRepository;
import com.example.moodtail.domain.moodtest.entity.MoodTestResult;
import com.example.moodtail.domain.moodtest.repository.MoodTestResultRepository;
import com.example.moodtail.domain.recommendation.calculator.TasteContributionCalculator;
import com.example.moodtail.domain.recommendation.calculator.TasteSimilarityCalculator;
import com.example.moodtail.domain.recommendation.dto.response.PairRecommendationResponse;
import com.example.moodtail.domain.recommendation.dto.response.RecommendedCocktailResponse;
import com.example.moodtail.domain.recommendation.model.RecommendationItemCommand;
import com.example.moodtail.domain.recommendation.model.TasteMetricContribution;
import com.example.moodtail.domain.recommendation.model.TasteProfile;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.service.InviteCodeService;
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

    private static final int RECOMMENDATION_LIMIT = 3;

    private final MoodTestResultRepository moodTestResultRepository;
    private final CocktailRepository cocktailRepository;
    private final TasteSimilarityCalculator tasteSimilarityCalculator;
    private final TasteContributionCalculator tasteContributionCalculator;
    private final PairRecommendationPersistenceService pairRecommendationPersistenceService;
    private final InviteCodeService inviteCodeService;

    public PairRecommendationResponse recommendPair(Long userId, String partnerInviteCode) {
        PairParticipants participants = validatePairRecommendationAvailable(userId, partnerInviteCode);
        MoodTestResult myResult = participants.myResult();
        MoodTestResult partnerResult = participants.partnerResult();

        TasteProfile myProfile = myResult.toTasteProfile();
        TasteProfile partnerProfile = partnerResult.toTasteProfile();
        TasteProfile compromise = myProfile.average(partnerProfile);

        RecommendationResult recommendationResult = recommend(compromise, myProfile, partnerProfile);
        List<RecommendedCocktailResponse> recommendations = recommendationResult.responses();

        List<TasteMetricContribution> tasteContributions = tasteContributionCalculator.calculate(
                myProfile,
                partnerProfile,
                recommendationResult.topCocktailProfile()
        );

        pairRecommendationPersistenceService.saveCompromise(
                myResult.getUser(),
                myResult,
                partnerResult,
                toCommands(recommendations)
        );

        return PairRecommendationResponse.of(
                myResult.getUser().getNickname(),
                partnerResult.getUser().getNickname(),
                myProfile,
                partnerProfile,
                compromise,
                recommendations,
                tasteContributions
        );
    }

    @Transactional(readOnly = true)
    public PairParticipants validatePairRecommendationAvailable(Long userId, String partnerInviteCode) {
        MoodTestResult myResult = findLatestResult(userId);
        User partner = inviteCodeService.findUserByInviteCode(partnerInviteCode);
        MoodTestResult partnerResult = findLatestResult(partner.getId());
        return new PairParticipants(myResult, partner, partnerResult);
    }

    private MoodTestResult findLatestResult(Long userId) {
        return moodTestResultRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new RestApiException(MoodTestErrorStatus.MOOD_TEST_RESULT_NOT_FOUND));
    }

    private RecommendationResult recommend(TasteProfile compromise, TasteProfile myProfile, TasteProfile partnerProfile) {
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
            TasteProfile cocktailProfile = scoredCocktail.cocktail().toTasteProfile();

            int matchScore = tasteSimilarityCalculator.calculateMatchScore(scoredCocktail.distance());
            int myMatchScore = tasteSimilarityCalculator.calculateMatchScore(
                    tasteSimilarityCalculator.calculateDistance(myProfile, cocktailProfile)
            );
            int partnerMatchScore = tasteSimilarityCalculator.calculateMatchScore(
                    tasteSimilarityCalculator.calculateDistance(partnerProfile, cocktailProfile)
            );

            responses.add(RecommendedCocktailResponse.of(
                    scoredCocktail.cocktail(), i + 1, matchScore, myMatchScore, partnerMatchScore
            ));
        }

        TasteProfile topCocktailProfile = scored.get(0).cocktail().toTasteProfile();
        return new RecommendationResult(responses, topCocktailProfile);
    }

    private List<RecommendationItemCommand> toCommands(List<RecommendedCocktailResponse> recommendations) {
        return recommendations.stream()
                .map(r -> new RecommendationItemCommand(r.cocktailId(), r.matchScore()))
                .toList();
    }

    private record ScoredCocktail(Cocktail cocktail, double distance) {
    }

    private record RecommendationResult(List<RecommendedCocktailResponse> responses, TasteProfile topCocktailProfile) {
    }

    public record PairParticipants(MoodTestResult myResult, User partner, MoodTestResult partnerResult) {
    }
}
