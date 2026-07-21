package com.example.moodtail.domain.moodtest.service;

import com.example.moodtail.domain.moodtest.dto.request.MoodTestResultRequest;
import com.example.moodtail.domain.moodtest.dto.response.MoodTestResultResponse;
import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.moodtest.entity.CompatibilityType;
import com.example.moodtail.domain.moodtest.entity.MoodQuestionOption;
import com.example.moodtail.domain.moodtest.entity.MoodQuestionType;
import com.example.moodtail.domain.moodtest.entity.MoodType;
import com.example.moodtail.domain.moodtest.entity.MoodTypeCompatibility;
import com.example.moodtail.domain.cocktail.repository.CocktailRepository;
import com.example.moodtail.domain.moodtest.repository.MoodQuestionOptionRepository;
import com.example.moodtail.domain.moodtest.repository.MoodTypeCompatibilityRepository;
import com.example.moodtail.domain.moodtest.repository.MoodTypeRepository;
import com.example.moodtail.domain.recommendation.calculator.TasteProfileCalculator;
import com.example.moodtail.domain.recommendation.calculator.TasteSimilarityCalculator;
import com.example.moodtail.domain.recommendation.model.TasteProfile;
import com.example.moodtail.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.moodtail.global.common.exception.code.status.MoodTestErrorStatus.MOOD_TEST_INVALID_ANSWER;
import static com.example.moodtail.global.common.exception.code.status.MoodTestErrorStatus.MOOD_TEST_QUESTION_OR_OPTION_NOT_FOUND;
import static com.example.moodtail.global.common.exception.code.status.RecommendationErrorStatus.RECOMMENDATION_UNAVAILABLE;

@Service
@RequiredArgsConstructor
public class MoodTestResultService {

    private static final int REQUIRED_ANSWER_COUNT = 7;
    private static final int REQUIRED_FIXED_ANSWER_COUNT = 5;
    private static final int REQUIRED_RANDOM_ANSWER_COUNT = 2;
    private static final int RECOMMENDATION_LIMIT = 4;

    private final MoodQuestionOptionRepository moodQuestionOptionRepository;
    private final MoodTypeRepository moodTypeRepository;
    private final CocktailRepository cocktailRepository;
    private final MoodTypeCompatibilityRepository moodTypeCompatibilityRepository;
    private final TasteProfileCalculator tasteProfileCalculator;
    private final TasteSimilarityCalculator tasteSimilarityCalculator;

    @Transactional(readOnly = true)
    public MoodTestResultResponse calculateResult(MoodTestResultRequest request) {
        validateAnswerFormat(request.answers());

        List<MoodQuestionOption> selectedOptions = getSelectedOptions(request.answers());
        validateQuestionOptionRelations(request.answers(), selectedOptions);

        List<MoodQuestionOption> fixedOptions = filterByQuestionType(selectedOptions, MoodQuestionType.FIXED);
        List<MoodQuestionOption> randomOptions = filterByQuestionType(selectedOptions, MoodQuestionType.RANDOM);
        validateQuestionTypeCounts(fixedOptions, randomOptions);

        TasteProfile userTasteProfile = calculateTasteProfile(fixedOptions, randomOptions);
        return calculateResult(userTasteProfile);
    }

    @Transactional(readOnly = true)
    public MoodTestResultResponse calculateResult(TasteProfile userTasteProfile) {
        MoodType matchedMoodType = findMatchedMoodType(userTasteProfile);
        List<RecommendationWithScore> recommendations = findRecommendations(userTasteProfile, matchedMoodType);
        MoodType bestMoodType = findCompatibility(matchedMoodType, CompatibilityType.BEST);
        MoodType worstMoodType = findCompatibility(matchedMoodType, CompatibilityType.WORST);

        return MoodTestResultResponse.builder()
                .resultId(null)
                .saved(false)
                .moodType(MoodTestResultResponse.moodTypeDto(matchedMoodType))
                .tasteProfile(MoodTestResultResponse.tasteProfileDto(userTasteProfile))
                .displayTasteScores(MoodTestResultResponse.displayTasteScoresDto(userTasteProfile))
                .recommendations(toRecommendationDtos(recommendations))
                .compatibilities(MoodTestResultResponse.CompatibilityDto.builder()
                        .best(MoodTestResultResponse.moodTypeDto(bestMoodType))
                        .worst(MoodTestResultResponse.moodTypeDto(worstMoodType))
                        .build())
                .build();
    }

    private void validateAnswerFormat(List<MoodTestResultRequest.AnswerDto> answers) {
        if (answers == null || answers.size() != REQUIRED_ANSWER_COUNT) {
            throw new RestApiException(MOOD_TEST_INVALID_ANSWER);
        }

        Set<Long> questionIds = new HashSet<>();
        Set<Long> optionIds = new HashSet<>();
        for (MoodTestResultRequest.AnswerDto answer : answers) {
            if (answer.questionId() == null || answer.optionId() == null) {
                throw new RestApiException(MOOD_TEST_INVALID_ANSWER);
            }
            if (!questionIds.add(answer.questionId()) || !optionIds.add(answer.optionId())) {
                throw new RestApiException(MOOD_TEST_INVALID_ANSWER);
            }
        }
    }

    private List<MoodQuestionOption> getSelectedOptions(List<MoodTestResultRequest.AnswerDto> answers) {
        List<Long> optionIds = answers.stream()
                .map(MoodTestResultRequest.AnswerDto::optionId)
                .toList();
        List<MoodQuestionOption> selectedOptions = moodQuestionOptionRepository.findDistinctByIdIn(optionIds);

        if (selectedOptions.size() != REQUIRED_ANSWER_COUNT) {
            throw new RestApiException(MOOD_TEST_QUESTION_OR_OPTION_NOT_FOUND);
        }
        return selectedOptions;
    }

    private void validateQuestionOptionRelations(
            List<MoodTestResultRequest.AnswerDto> answers,
            List<MoodQuestionOption> selectedOptions
    ) {
        Map<Long, MoodQuestionOption> optionMap = selectedOptions.stream()
                .collect(Collectors.toMap(MoodQuestionOption::getId, Function.identity()));

        for (MoodTestResultRequest.AnswerDto answer : answers) {
            MoodQuestionOption option = optionMap.get(answer.optionId());
            if (option == null || !option.getMoodQuestion().getId().equals(answer.questionId())) {
                throw new RestApiException(MOOD_TEST_QUESTION_OR_OPTION_NOT_FOUND);
            }
            if (!Boolean.TRUE.equals(option.getMoodQuestion().getIsActive())) {
                throw new RestApiException(MOOD_TEST_QUESTION_OR_OPTION_NOT_FOUND);
            }
        }
    }

    private List<MoodQuestionOption> filterByQuestionType(
            List<MoodQuestionOption> options,
            MoodQuestionType questionType
    ) {
        return options.stream()
                .filter(option -> option.getMoodQuestion().getQuestionType() == questionType)
                .sorted(Comparator
                        .comparing((MoodQuestionOption option) -> option.getMoodQuestion().getSortOrder())
                        .thenComparing(MoodQuestionOption::getId))
                .toList();
    }

    private void validateQuestionTypeCounts(
            List<MoodQuestionOption> fixedOptions,
            List<MoodQuestionOption> randomOptions
    ) {
        if (fixedOptions.size() != REQUIRED_FIXED_ANSWER_COUNT || randomOptions.size() != REQUIRED_RANDOM_ANSWER_COUNT) {
            throw new RestApiException(MOOD_TEST_INVALID_ANSWER);
        }

        Set<Integer> fixedSortOrders = fixedOptions.stream()
                .map(option -> option.getMoodQuestion().getSortOrder())
                .collect(Collectors.toSet());
        if (!fixedSortOrders.equals(Set.of(1, 2, 3, 4, 5))) {
            throw new RestApiException(MOOD_TEST_INVALID_ANSWER);
        }
    }

    private TasteProfile calculateTasteProfile(
            List<MoodQuestionOption> fixedOptions,
            List<MoodQuestionOption> randomOptions
    ) {
        try {
            return tasteProfileCalculator.calculate(fixedOptions, randomOptions);
        } catch (IllegalStateException e) {
            throw new RestApiException(RECOMMENDATION_UNAVAILABLE);
        }
    }

    private MoodType findMatchedMoodType(TasteProfile userTasteProfile) {
        return moodTypeRepository.findAllByOrderBySortOrderAscIdAsc().stream()
                .min(Comparator
                        .comparingDouble((MoodType moodType) ->
                                tasteSimilarityCalculator.calculateDistance(userTasteProfile, moodType.toTasteProfile()))
                        .thenComparing(MoodType::getSortOrder)
                        .thenComparing(MoodType::getId))
                .orElseThrow(() -> new RestApiException(RECOMMENDATION_UNAVAILABLE));
    }

    private List<RecommendationWithScore> findRecommendations(TasteProfile userTasteProfile, MoodType matchedMoodType) {
        List<Cocktail> cocktails = cocktailRepository.findByMoodTypeId(matchedMoodType.getId());
        if (cocktails.size() < RECOMMENDATION_LIMIT) {
            throw new RestApiException(RECOMMENDATION_UNAVAILABLE);
        }

        return cocktails.stream()
                .map(cocktail -> {
                    double distance = tasteSimilarityCalculator.calculateDistance(userTasteProfile, cocktail.toTasteProfile());
                    int matchScore = tasteSimilarityCalculator.calculateMatchScore(distance);
                    return new RecommendationWithScore(cocktail, distance, matchScore);
                })
                .sorted(Comparator
                        .comparingDouble(RecommendationWithScore::distance)
                        .thenComparing(recommendation -> recommendation.cocktail().getId()))
                .limit(RECOMMENDATION_LIMIT)
                .toList();
    }

    private MoodType findCompatibility(MoodType moodType, CompatibilityType compatibilityType) {
        return moodTypeCompatibilityRepository.findByMoodTypeIdAndCompatibilityType(moodType.getId(), compatibilityType)
                .map(MoodTypeCompatibility::getTargetMoodType)
                .orElseThrow(() -> new RestApiException(RECOMMENDATION_UNAVAILABLE));
    }

    private List<MoodTestResultResponse.RecommendationDto> toRecommendationDtos(
            List<RecommendationWithScore> recommendations
    ) {
        List<MoodTestResultResponse.RecommendationDto> result = new ArrayList<>();
        for (int i = 0; i < recommendations.size(); i++) {
            RecommendationWithScore recommendation = recommendations.get(i);
            result.add(MoodTestResultResponse.recommendationDto(
                    i + 1,
                    recommendation.cocktail(),
                    recommendation.matchScore()
            ));
        }
        return result;
    }

    private record RecommendationWithScore(
            Cocktail cocktail,
            double distance,
            int matchScore
    ) {
    }
}
