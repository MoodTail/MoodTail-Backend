package com.example.moodtail.domain.recommendation.service;

import com.example.moodtail.domain.moodtest.entity.Cocktail;
import com.example.moodtail.domain.moodtest.entity.MoodTestResult;
import com.example.moodtail.domain.moodtest.repository.CocktailRepository;
import com.example.moodtail.domain.recommendation.entity.RecommendationItem;
import com.example.moodtail.domain.recommendation.entity.RecommendationSession;
import com.example.moodtail.domain.recommendation.entity.RecommendationSessionType;
import com.example.moodtail.domain.recommendation.model.RecommendationItemCommand;
import com.example.moodtail.domain.recommendation.repository.RecommendationItemRepository;
import com.example.moodtail.domain.recommendation.repository.RecommendationSessionRepository;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.moodtail.global.common.exception.code.status.MoodTestErrorStatus.MOOD_TEST_COCKTAIL_NOT_FOUND;
import static com.example.moodtail.global.common.exception.code.status.MoodTestErrorStatus.MOOD_TEST_INVALID_RESULT;

@Service
@RequiredArgsConstructor
public class RecommendationPersistenceService {

    private static final int REQUIRED_RECOMMENDATION_COUNT = 4;

    private final CocktailRepository cocktailRepository;
    private final RecommendationSessionRepository recommendationSessionRepository;
    private final RecommendationItemRepository recommendationItemRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void replaceTestResultRecommendation(
            User user,
            MoodTestResult moodTestResult,
            List<RecommendationItemCommand> commands
    ) {
        List<Cocktail> cocktails = getCocktails(commands);
        deleteExistingRecommendation(moodTestResult.getId());

        RecommendationSession recommendationSession = recommendationSessionRepository.save(
                RecommendationSession.forTestResult(user, moodTestResult)
        );
        Map<Long, Cocktail> cocktailMap = cocktails.stream()
                .collect(Collectors.toMap(Cocktail::getId, Function.identity()));

        List<RecommendationItem> items = new ArrayList<>(commands.size());
        for (int i = 0; i < commands.size(); i++) {
            RecommendationItemCommand command = commands.get(i);
            Cocktail cocktail = cocktailMap.get(command.cocktailId());
            if (cocktail == null) {
                throw new RestApiException(MOOD_TEST_COCKTAIL_NOT_FOUND);
            }
            items.add(RecommendationItem.create(
                    recommendationSession,
                    cocktail,
                    i + 1,
                    command.matchScore()
            ));
        }
        recommendationItemRepository.saveAll(items);
    }

    private List<Cocktail> getCocktails(List<RecommendationItemCommand> commands) {
        if (commands.size() != REQUIRED_RECOMMENDATION_COUNT) {
            throw new RestApiException(MOOD_TEST_INVALID_RESULT);
        }

        List<Long> cocktailIds = commands.stream()
                .map(RecommendationItemCommand::cocktailId)
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

    private void deleteExistingRecommendation(Long moodTestResultId) {
        List<RecommendationSession> oldSessions = recommendationSessionRepository.findByMoodTestResultIdAndSessionType(
                moodTestResultId,
                RecommendationSessionType.TEST_RESULT
        );
        if (oldSessions.isEmpty()) {
            return;
        }
        recommendationItemRepository.deleteByRecommendationSessionIn(oldSessions);
        recommendationSessionRepository.deleteAll(oldSessions);
    }
}
