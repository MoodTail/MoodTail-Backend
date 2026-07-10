package com.example.moodtail.domain.moodtest.service;

import com.example.moodtail.domain.moodtest.dto.request.MoodTestResultSaveRequest;
import com.example.moodtail.domain.moodtest.entity.Cocktail;
import com.example.moodtail.domain.moodtest.entity.MoodTestResult;
import com.example.moodtail.domain.moodtest.entity.MoodType;
import com.example.moodtail.domain.moodtest.entity.RecommendationItem;
import com.example.moodtail.domain.moodtest.entity.RecommendationSession;
import com.example.moodtail.domain.moodtest.entity.RecommendationSessionType;
import com.example.moodtail.domain.moodtest.repository.CocktailRepository;
import com.example.moodtail.domain.moodtest.repository.MoodTestResultRepository;
import com.example.moodtail.domain.moodtest.repository.MoodTypeRepository;
import com.example.moodtail.domain.moodtest.repository.RecommendationItemRepository;
import com.example.moodtail.domain.moodtest.repository.RecommendationSessionRepository;
import com.example.moodtail.domain.recommendation.model.TasteProfile;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.USER_NOT_FOUND;
import static com.example.moodtail.global.common.exception.code.status.MoodTestErrorStatus.MOOD_TEST_COCKTAIL_NOT_FOUND;
import static com.example.moodtail.global.common.exception.code.status.MoodTestErrorStatus.MOOD_TEST_INVALID_RESULT;
import static com.example.moodtail.global.common.exception.code.status.MoodTestErrorStatus.MOOD_TEST_MOOD_TYPE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class MoodTestResultSaveService {

    private static final int REQUIRED_RECOMMENDATION_COUNT = 4;
    private static final ZoneId RESULT_DATE_ZONE = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final MoodTypeRepository moodTypeRepository;
    private final CocktailRepository cocktailRepository;
    private final MoodTestResultRepository moodTestResultRepository;
    private final RecommendationSessionRepository recommendationSessionRepository;
    private final RecommendationItemRepository recommendationItemRepository;

    @Transactional
    public Long saveResult(Long userId, MoodTestResultSaveRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(USER_NOT_FOUND));
        MoodType moodType = getMoodType(request.moodType());
        TasteProfile tasteProfile = toTasteProfile(request.tasteProfile());
        List<Cocktail> recommendedCocktails = getRecommendedCocktails(request.recommendedCocktails());

        LocalDate resultDate = LocalDate.now(RESULT_DATE_ZONE);
        MoodTestResult moodTestResult = moodTestResultRepository.findByUserIdAndResultDate(userId, resultDate)
                .map(existingResult -> {
                    existingResult.updateResult(moodType, tasteProfile);
                    return existingResult;
                })
                .orElseGet(() -> MoodTestResult.create(user, moodType, resultDate, tasteProfile));

        MoodTestResult savedResult = moodTestResultRepository.save(moodTestResult);
        replaceRecommendation(user, savedResult, request.recommendedCocktails(), recommendedCocktails);

        return savedResult.getId();
    }

    private MoodType getMoodType(MoodTestResultSaveRequest.MoodTypeDto requestMoodType) {
        MoodType moodType = moodTypeRepository.findById(requestMoodType.moodTypeId())
                .orElseThrow(() -> new RestApiException(MOOD_TEST_MOOD_TYPE_NOT_FOUND));

        String typeCode = requestMoodType.typeCode();
        if (typeCode != null && !typeCode.isBlank() && !moodType.getCode().equals(typeCode)) {
            throw new RestApiException(MOOD_TEST_INVALID_RESULT);
        }
        return moodType;
    }

    private TasteProfile toTasteProfile(MoodTestResultSaveRequest.TasteProfileDto tasteProfile) {
        return TasteProfile.of(
                tasteProfile.alcoholIntensity(),
                tasteProfile.sweetness(),
                tasteProfile.sourness(),
                tasteProfile.refreshing(),
                tasteProfile.bitterness()
        );
    }

    private List<Cocktail> getRecommendedCocktails(
            List<MoodTestResultSaveRequest.RecommendedCocktailDto> recommendedCocktails
    ) {
        if (recommendedCocktails.size() != REQUIRED_RECOMMENDATION_COUNT) {
            throw new RestApiException(MOOD_TEST_INVALID_RESULT);
        }

        List<Long> cocktailIds = recommendedCocktails.stream()
                .map(MoodTestResultSaveRequest.RecommendedCocktailDto::cocktailId)
                .toList();
        Set<Long> distinctCocktailIds = new HashSet<>(cocktailIds);
        if (distinctCocktailIds.size() != REQUIRED_RECOMMENDATION_COUNT) {
            throw new RestApiException(MOOD_TEST_INVALID_RESULT);
        }

        List<Cocktail> cocktails = cocktailRepository.findAllById(cocktailIds);
        if (cocktails.size() != REQUIRED_RECOMMENDATION_COUNT) {
            throw new RestApiException(MOOD_TEST_COCKTAIL_NOT_FOUND);
        }
        return cocktails;
    }

    private void replaceRecommendation(
            User user,
            MoodTestResult moodTestResult,
            List<MoodTestResultSaveRequest.RecommendedCocktailDto> recommendationRequests,
            List<Cocktail> recommendedCocktails
    ) {
        List<RecommendationSession> oldSessions = recommendationSessionRepository.findByMoodTestResultIdAndSessionType(
                moodTestResult.getId(),
                RecommendationSessionType.TEST_RESULT
        );
        if (!oldSessions.isEmpty()) {
            recommendationItemRepository.deleteByRecommendationSessionIn(oldSessions);
            recommendationSessionRepository.deleteAll(oldSessions);
        }

        RecommendationSession recommendationSession = recommendationSessionRepository.save(
                RecommendationSession.forTestResult(user, moodTestResult)
        );
        Map<Long, Cocktail> cocktailMap = recommendedCocktails.stream()
                .collect(Collectors.toMap(Cocktail::getId, Function.identity()));

        for (int i = 0; i < recommendationRequests.size(); i++) {
            MoodTestResultSaveRequest.RecommendedCocktailDto recommendationRequest = recommendationRequests.get(i);
            Cocktail cocktail = cocktailMap.get(recommendationRequest.cocktailId());
            if (cocktail == null) {
                throw new RestApiException(MOOD_TEST_COCKTAIL_NOT_FOUND);
            }
            recommendationItemRepository.save(RecommendationItem.create(
                    recommendationSession,
                    cocktail,
                    i + 1,
                    recommendationRequest.matchScore()
            ));
        }
    }
}
