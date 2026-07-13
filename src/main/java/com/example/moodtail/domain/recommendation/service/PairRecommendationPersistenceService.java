package com.example.moodtail.domain.recommendation.service;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.cocktail.repository.CocktailRepository;
import com.example.moodtail.domain.moodtest.entity.MoodTestResult;
import com.example.moodtail.domain.recommendation.entity.RecommendationItem;
import com.example.moodtail.domain.recommendation.entity.RecommendationSession;
import com.example.moodtail.domain.recommendation.model.RecommendationItemCommand;
import com.example.moodtail.domain.recommendation.repository.RecommendationItemRepository;
import com.example.moodtail.domain.recommendation.repository.RecommendationSessionRepository;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.moodtail.global.common.exception.code.status.MoodTestErrorStatus.MOOD_TEST_COCKTAIL_NOT_FOUND;
import static com.example.moodtail.global.common.exception.code.status.RecommendationErrorStatus.RECOMMENDATION_UNAVAILABLE;

@Service
@RequiredArgsConstructor
public class PairRecommendationPersistenceService {
    private static final int REQUIRED_RECOMMENDATION_COUNT = 4;

    private final CocktailRepository cocktailRepository;
    private final RecommendationItemRepository recommendationItemRepository;
    private final RecommendationSessionRepository recommendationSessionRepository;

    @Transactional
    public void saveCompromise(
            User user,
            MoodTestResult moodTestResult,
            MoodTestResult partnerMoodTestResult,
            List<RecommendationItemCommand> commands
    ){
        if (commands.size() != REQUIRED_RECOMMENDATION_COUNT) {
            throw new RestApiException(RECOMMENDATION_UNAVAILABLE);
        }

        List<Long> cocktailIds = commands.stream()
                .map(RecommendationItemCommand::cocktailId)
                .toList();

        Map<Long, Cocktail> cocktailMap = cocktailRepository.findAllById(cocktailIds).stream()
                .collect(Collectors.toMap(Cocktail::getId, Function.identity()));

        RecommendationSession session = recommendationSessionRepository.save(
                RecommendationSession.forCompromise(user, moodTestResult, partnerMoodTestResult)
        );

        List<RecommendationItem> items = new ArrayList<>(commands.size());
        for (int i = 0; i < commands.size(); i++) {
            RecommendationItemCommand command = commands.get(i);
            Cocktail cocktail = cocktailMap.get(command.cocktailId());
            if (cocktail == null) {
                throw new RestApiException(MOOD_TEST_COCKTAIL_NOT_FOUND);
            }
            items.add(RecommendationItem.create(
                    session,
                    cocktail,
                    i + 1,
                    command.matchScore()
            ));
        }
        recommendationItemRepository.saveAll(items);
    }
}
