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
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.MoodTestErrorStatus;
import com.example.moodtail.global.common.exception.code.status.RecommendationErrorStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class PairRecommendationServiceTest {

    @Mock
    private MoodTestResultRepository moodTestResultRepository;

    @Mock
    private CocktailRepository cocktailRepository;

    @Mock
    private PairRecommendationPersistenceService pairRecommendationPersistenceService;

    private final TasteSimilarityCalculator tasteSimilarityCalculator = new TasteSimilarityCalculator();

    private PairRecommendationService service;

    @BeforeEach
    void setUp() {
        service = new PairRecommendationService(
                moodTestResultRepository,
                cocktailRepository,
                tasteSimilarityCalculator,
                pairRecommendationPersistenceService
        );
    }

    @Test
    void recommendsPairByResultIdAndSavesSortedByDistance() {
        User me = userWithId(1L);
        TasteProfile myProfile = TasteProfile.of(
                new BigDecimal("2.0"), new BigDecimal("2.0"), new BigDecimal("2.0"),
                new BigDecimal("2.0"), new BigDecimal("2.0")
        );
        TasteProfile partnerProfile = TasteProfile.of(
                new BigDecimal("4.0"), new BigDecimal("4.0"), new BigDecimal("4.0"),
                new BigDecimal("4.0"), new BigDecimal("4.0")
        );
        User partner = userWithId(2L);
        MoodTestResult myResult = resultOwnedBy(me, 10L, myProfile);
        MoodTestResult partnerResult = resultWithShareToken(partner, "partner-share-token", partnerProfile);

        TasteProfile compromise = myProfile.average(partnerProfile);

        Cocktail perfectMatch = cocktail(101L, "완벽매치", "Perfect Match", compromise);
        Cocktail close = cocktail(102L, "근접매치", "Close Match",
                new BigDecimal("3.5"), new BigDecimal("3.0"), new BigDecimal("3.0"),
                new BigDecimal("3.0"), new BigDecimal("3.0"));
        Cocktail farther = cocktail(103L, "먼매치", "Farther Match",
                new BigDecimal("2.0"), new BigDecimal("3.0"), new BigDecimal("3.0"),
                new BigDecimal("3.0"), new BigDecimal("3.0"));
        Cocktail farthest = cocktail(104L, "가장먼매치", "Farthest Match",
                new BigDecimal("1.0"), new BigDecimal("3.0"), new BigDecimal("3.0"),
                new BigDecimal("3.0"), new BigDecimal("3.0"));
        Cocktail excluded = cocktail(105L, "제외됨", "Excluded",
                new BigDecimal("5.0"), new BigDecimal("5.0"), new BigDecimal("5.0"),
                new BigDecimal("5.0"), new BigDecimal("5.0"));

        when(moodTestResultRepository.findById(10L)).thenReturn(Optional.of(myResult));
        when(moodTestResultRepository.findByShareToken("partner-share-token")).thenReturn(Optional.of(partnerResult));
        when(cocktailRepository.findAll())
                .thenReturn(List.of(excluded, farthest, close, perfectMatch, farther));

        PairRecommendationResponse response = service.recommendPair(1L, 10L, null, "partner-share-token");

        assertThat(response.recommendationSaved()).isTrue();
        assertThat(response.compromiseProfile()).isEqualTo(CompromiseProfileResponse.from(compromise));

        List<RecommendedCocktailResponse> recommendations = response.recommendations();
        assertThat(recommendations).hasSize(4);
        assertThat(recommendations)
                .extracting(RecommendedCocktailResponse::cocktailId)
                .containsExactly(101L, 102L, 103L, 104L);
        assertThat(recommendations)
                .extracting(RecommendedCocktailResponse::ranking)
                .containsExactly(1, 2, 3, 4);
        assertThat(recommendations)
                .extracting(RecommendedCocktailResponse::matchScore)
                .isSortedAccordingTo((a, b) -> b - a);
        assertThat(recommendations.get(0).matchScore()).isEqualTo(100);

        List<Cocktail> candidates = List.of(perfectMatch, close, farther, farthest);
        for (RecommendedCocktailResponse recommendation : recommendations) {
            Cocktail matching = candidates.stream()
                    .filter(c -> c.getId().equals(recommendation.cocktailId()))
                    .findFirst()
                    .orElseThrow();
            double distance = tasteSimilarityCalculator.calculateDistance(compromise, matching.toTasteProfile());
            int expectedScore = tasteSimilarityCalculator.calculateMatchScore(distance);
            assertThat(recommendation.matchScore()).isEqualTo(expectedScore);
        }

        ArgumentCaptor<List<RecommendationItemCommand>> commandsCaptor = ArgumentCaptor.forClass(List.class);
        verify(pairRecommendationPersistenceService).saveCompromise(
                eq(me), eq(myResult), eq(partnerResult), commandsCaptor.capture()
        );
        assertThat(commandsCaptor.getValue())
                .extracting(RecommendationItemCommand::cocktailId)
                .containsExactly(101L, 102L, 103L, 104L);
    }

    @Test
    void recommendsPairByShareTokenWithoutSavingRecommendation() {
        User me = userWithId(1L);
        User partner = userWithId(2L);
        TasteProfile myProfile = TasteProfile.of(
                new BigDecimal("3.0"), new BigDecimal("3.0"), new BigDecimal("3.0"),
                new BigDecimal("3.0"), new BigDecimal("3.0")
        );
        TasteProfile partnerProfile = TasteProfile.of(
                new BigDecimal("1.0"), new BigDecimal("1.0"), new BigDecimal("1.0"),
                new BigDecimal("1.0"), new BigDecimal("1.0")
        );
        MoodTestResult myResult = resultWithShareToken(me, "my-share-token", myProfile);
        MoodTestResult partnerResult = resultWithShareToken(partner, "partner-share-token", partnerProfile);

        when(moodTestResultRepository.findByShareToken("my-share-token")).thenReturn(Optional.of(myResult));
        when(moodTestResultRepository.findByShareToken("partner-share-token")).thenReturn(Optional.of(partnerResult));
        when(cocktailRepository.findAll()).thenReturn(List.of(
                cocktail(201L, "A", "A", new BigDecimal("2.0"), new BigDecimal("2.0"),
                        new BigDecimal("2.0"), new BigDecimal("2.0"), new BigDecimal("2.0")),
                cocktail(202L, "B", "B", new BigDecimal("2.1"), new BigDecimal("2.0"),
                        new BigDecimal("2.0"), new BigDecimal("2.0"), new BigDecimal("2.0")),
                cocktail(203L, "C", "C", new BigDecimal("2.2"), new BigDecimal("2.0"),
                        new BigDecimal("2.0"), new BigDecimal("2.0"), new BigDecimal("2.0")),
                cocktail(204L, "D", "D", new BigDecimal("2.3"), new BigDecimal("2.0"),
                        new BigDecimal("2.0"), new BigDecimal("2.0"), new BigDecimal("2.0"))
        ));

        PairRecommendationResponse response =
                service.recommendPair(null, null, "my-share-token", "partner-share-token");

        assertThat(response.recommendationSaved()).isFalse();
        verify(pairRecommendationPersistenceService, never())
                .saveCompromise(any(), any(), any(), any());
    }

    @Test
    void throwsInvalidParameterWhenNeitherResultIdNorShareTokenIsProvided() {
        assertThatThrownBy(() -> service.recommendPair(1L, null, null, "partner-share-token"))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode())
                                .isEqualTo(RecommendationErrorStatus.RECOMMENDATION_INVALID_PARAMETER.getCode().getCode())
                );
    }

    @Test
    void throwsResultNotFoundWhenResultIdDoesNotExist() {
        when(moodTestResultRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.recommendPair(1L, 999L, null, "partner-share-token"))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode())
                                .isEqualTo(MoodTestErrorStatus.MOOD_TEST_RESULT_NOT_FOUND.getCode().getCode())
                );
    }

    @Test
    void throwsInvalidParameterWhenPartnerShareTokenIsMissing() {
        User me = userWithId(1L);
        TasteProfile myProfile = TasteProfile.of(
                new BigDecimal("2.0"), new BigDecimal("2.0"), new BigDecimal("2.0"),
                new BigDecimal("2.0"), new BigDecimal("2.0")
        );
        MoodTestResult myResult = resultOwnedBy(me, 10L, myProfile);
        when(moodTestResultRepository.findById(10L)).thenReturn(Optional.of(myResult));

        assertThatThrownBy(() -> service.recommendPair(1L, 10L, null, null))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode())
                                .isEqualTo(RecommendationErrorStatus.RECOMMENDATION_INVALID_PARAMETER.getCode().getCode())
                );
    }

    @Test
    void returnsOnlyAvailableCocktailsWhenFewerThanRecommendationLimitExist() {
        // TODO: 추천 후보 칵테일이 4개 미만일 때의 정책은 아직 정해지지 않았다. 현재 구현은 있는 만큼만
        // 반환하고 그대로 저장을 시도하며, 실제 개수 검증(4개 고정)은 PairRecommendationPersistenceService
        // 쪽 책임으로 넘어가 있다(여기서는 mock이라 실패하지 않음). 정책이 정해지면 이 테스트를 갱신할 것.
        User me = userWithId(1L);
        User partner = userWithId(2L);
        TasteProfile myProfile = TasteProfile.of(
                new BigDecimal("3.0"), new BigDecimal("3.0"), new BigDecimal("3.0"),
                new BigDecimal("3.0"), new BigDecimal("3.0")
        );
        MoodTestResult myResult = resultOwnedBy(me, 10L, myProfile);
        MoodTestResult partnerResult = resultWithShareToken(partner, "partner-share-token", myProfile);

        when(moodTestResultRepository.findById(10L)).thenReturn(Optional.of(myResult));
        when(moodTestResultRepository.findByShareToken("partner-share-token")).thenReturn(Optional.of(partnerResult));
        when(cocktailRepository.findAll()).thenReturn(List.of(
                cocktail(301L, "A", "A", new BigDecimal("3.0"), new BigDecimal("3.0"),
                        new BigDecimal("3.0"), new BigDecimal("3.0"), new BigDecimal("3.0")),
                cocktail(302L, "B", "B", new BigDecimal("3.5"), new BigDecimal("3.0"),
                        new BigDecimal("3.0"), new BigDecimal("3.0"), new BigDecimal("3.0"))
        ));

        PairRecommendationResponse response = service.recommendPair(1L, 10L, null, "partner-share-token");

        assertThat(response.recommendations()).hasSize(2);
        assertThat(response.recommendations())
                .extracting(RecommendedCocktailResponse::ranking)
                .containsExactly(1, 2);
    }

    private User userWithId(long id) {
        User user = User.createGuest("guest-" + id, "user-" + id, LocalDateTime.now());
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private MoodTestResult resultOwnedBy(User owner, long id, TasteProfile profile) {
        MoodTestResult result = MoodTestResult.create(owner, null, LocalDate.now(), profile);
        ReflectionTestUtils.setField(result, "id", id);
        return result;
    }

    private MoodTestResult resultWithShareToken(User owner, String shareToken, TasteProfile profile) {
        MoodTestResult result = MoodTestResult.create(owner, null, LocalDate.now(), profile);
        ReflectionTestUtils.setField(result, "shareToken", shareToken);
        return result;
    }

    private Cocktail cocktail(Long id, String nameKo, String nameEn, TasteProfile profile) {
        return cocktail(id, nameKo, nameEn,
                profile.alcoholIntensity(), profile.sweetness(), profile.sourness(),
                profile.refreshing(), profile.bitterness());
    }

    private Cocktail cocktail(
            Long id, String nameKo, String nameEn,
            BigDecimal alcoholIntensity, BigDecimal sweetness, BigDecimal sourness,
            BigDecimal refreshing, BigDecimal bitterness
    ) {
        try {
            Constructor<Cocktail> constructor = Cocktail.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Cocktail cocktail = constructor.newInstance();
            ReflectionTestUtils.setField(cocktail, "id", id);
            ReflectionTestUtils.setField(cocktail, "nameKo", nameKo);
            ReflectionTestUtils.setField(cocktail, "nameEn", nameEn);
            ReflectionTestUtils.setField(cocktail, "alcoholIntensity", alcoholIntensity);
            ReflectionTestUtils.setField(cocktail, "sweetness", sweetness);
            ReflectionTestUtils.setField(cocktail, "sourness", sourness);
            ReflectionTestUtils.setField(cocktail, "refreshing", refreshing);
            ReflectionTestUtils.setField(cocktail, "bitterness", bitterness);
            return cocktail;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
