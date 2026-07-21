package com.example.moodtail.domain.recommendation.service;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.cocktail.repository.CocktailRepository;
import com.example.moodtail.domain.moodtest.entity.MoodTestResult;
import com.example.moodtail.domain.recommendation.entity.RecommendationSession;
import com.example.moodtail.domain.recommendation.model.RecommendationItemCommand;
import com.example.moodtail.domain.recommendation.repository.RecommendationItemRepository;
import com.example.moodtail.domain.recommendation.repository.RecommendationSessionRepository;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.RecommendationErrorStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PairRecommendationPersistenceServiceTest {

    @Mock
    private CocktailRepository cocktailRepository;

    @Mock
    private RecommendationItemRepository recommendationItemRepository;

    @Mock
    private RecommendationSessionRepository recommendationSessionRepository;

    private PairRecommendationPersistenceService service;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new PairRecommendationPersistenceService(
                cocktailRepository,
                recommendationItemRepository,
                recommendationSessionRepository
        );
    }

    @Test
    void savesSuccessfullyWhenFourCandidatesProvided() {
        User user = userWithId(1L);
        MoodTestResult myResult = moodTestResult(10L);
        MoodTestResult partnerResult = moodTestResult(20L);
        List<RecommendationItemCommand> commands = List.of(
                new RecommendationItemCommand(101L, 90),
                new RecommendationItemCommand(102L, 80),
                new RecommendationItemCommand(103L, 70),
                new RecommendationItemCommand(104L, 60)
        );

        when(cocktailRepository.findAllById(any())).thenReturn(List.of(
                cocktail(101L), cocktail(102L), cocktail(103L), cocktail(104L)
        ));
        when(recommendationSessionRepository.save(any()))
                .thenReturn(RecommendationSession.forCompromise(user, myResult, partnerResult));

        service.saveCompromise(user, myResult, partnerResult, commands);

        ArgumentCaptor<List<com.example.moodtail.domain.recommendation.entity.RecommendationItem>> itemsCaptor =
                ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(recommendationItemRepository).saveAll(itemsCaptor.capture());
        assertThat(itemsCaptor.getValue()).hasSize(4);
    }

    @Test
    void savesSuccessfullyWhenFewerThanFourCandidatesProvided() {
        User user = userWithId(1L);
        MoodTestResult myResult = moodTestResult(10L);
        MoodTestResult partnerResult = moodTestResult(20L);
        List<RecommendationItemCommand> commands = List.of(
                new RecommendationItemCommand(101L, 90),
                new RecommendationItemCommand(102L, 80),
                new RecommendationItemCommand(103L, 70)
        );

        when(cocktailRepository.findAllById(any())).thenReturn(List.of(
                cocktail(101L), cocktail(102L), cocktail(103L)
        ));
        when(recommendationSessionRepository.save(any()))
                .thenReturn(RecommendationSession.forCompromise(user, myResult, partnerResult));

        service.saveCompromise(user, myResult, partnerResult, commands);

        ArgumentCaptor<List<com.example.moodtail.domain.recommendation.entity.RecommendationItem>> itemsCaptor =
                ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(recommendationItemRepository).saveAll(itemsCaptor.capture());
        assertThat(itemsCaptor.getValue()).hasSize(3);
    }

    @Test
    void throwsRecommendationUnavailableWhenCandidatesEmpty() {
        User user = userWithId(1L);
        MoodTestResult myResult = moodTestResult(10L);
        MoodTestResult partnerResult = moodTestResult(20L);

        assertThatThrownBy(() ->
                service.saveCompromise(user, myResult, partnerResult, Collections.emptyList())
        ).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode())
                        .isEqualTo(RecommendationErrorStatus.RECOMMENDATION_UNAVAILABLE.getCode().getCode())
        );
    }

    private User userWithId(long id) {
        User user = User.createGuest("guest-" + id, "user-" + id, LocalDateTime.now());
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private MoodTestResult moodTestResult(long id) {
        MoodTestResult result = MoodTestResult.create(userWithId(id), null, java.time.LocalDate.now(),
                com.example.moodtail.domain.recommendation.model.TasteProfile.of(
                        new java.math.BigDecimal("2.0"), new java.math.BigDecimal("2.0"),
                        new java.math.BigDecimal("2.0"), new java.math.BigDecimal("2.0"),
                        new java.math.BigDecimal("2.0")
                ));
        ReflectionTestUtils.setField(result, "id", id);
        return result;
    }

    private Cocktail cocktail(Long id) {
        try {
            Constructor<Cocktail> constructor = Cocktail.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Cocktail cocktail = constructor.newInstance();
            ReflectionTestUtils.setField(cocktail, "id", id);
            return cocktail;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
