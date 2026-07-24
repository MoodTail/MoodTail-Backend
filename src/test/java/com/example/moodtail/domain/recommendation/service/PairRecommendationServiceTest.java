package com.example.moodtail.domain.recommendation.service;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.cocktail.repository.CocktailRepository;
import com.example.moodtail.domain.moodtest.entity.MoodTestResult;
import com.example.moodtail.domain.moodtest.repository.MoodTestResultRepository;
import com.example.moodtail.domain.recommendation.calculator.TasteContributionCalculator;
import com.example.moodtail.domain.recommendation.calculator.TasteSimilarityCalculator;
import com.example.moodtail.domain.recommendation.dto.response.CompromiseProfileResponse;
import com.example.moodtail.domain.recommendation.dto.response.PairRecommendationResponse;
import com.example.moodtail.domain.recommendation.dto.response.RecommendedCocktailResponse;
import com.example.moodtail.domain.recommendation.model.TasteProfile;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.service.InviteCodeService;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.common.exception.code.status.MoodTestErrorStatus;
import com.example.moodtail.global.common.exception.code.status.UserErrorStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PairRecommendationServiceTest {

    @Mock
    private MoodTestResultRepository moodTestResultRepository;

    @Mock
    private CocktailRepository cocktailRepository;

    @Mock
    private InviteCodeService inviteCodeService;

    private final TasteSimilarityCalculator tasteSimilarityCalculator = new TasteSimilarityCalculator();
    private final TasteContributionCalculator tasteContributionCalculator = new TasteContributionCalculator();

    private PairRecommendationService service;

    @BeforeEach
    void setUp() {
        service = new PairRecommendationService(
                moodTestResultRepository,
                cocktailRepository,
                tasteSimilarityCalculator,
                tasteContributionCalculator,
                inviteCodeService
        );
    }

    @Test
    void recommendsPairByPartnerInviteCode() {
        User me = userWithId(1L, "나닉네임");
        User partner = userWithId(2L, "상대닉네임");
        TasteProfile myProfile = TasteProfile.of(
                new BigDecimal("2.0"), new BigDecimal("2.0"), new BigDecimal("2.0"),
                new BigDecimal("2.0"), new BigDecimal("2.0")
        );
        TasteProfile partnerProfile = TasteProfile.of(
                new BigDecimal("4.0"), new BigDecimal("4.0"), new BigDecimal("4.0"),
                new BigDecimal("4.0"), new BigDecimal("4.0")
        );
        MoodTestResult myResult = latestResultOf(me, 10L, myProfile);
        MoodTestResult partnerResult = latestResultOf(partner, 20L, partnerProfile);

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

        when(moodTestResultRepository.findFirstByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.of(myResult));
        when(inviteCodeService.findUserByInviteCode("MOOD-4821")).thenReturn(partner);
        when(moodTestResultRepository.findFirstByUserIdOrderByCreatedAtDesc(2L)).thenReturn(Optional.of(partnerResult));
        when(cocktailRepository.findAll())
                .thenReturn(List.of(excluded, farthest, close, perfectMatch, farther));

        PairRecommendationResponse response = service.recommendPair(1L, "MOOD-4821");

        assertThat(response.myNickname()).isEqualTo("나닉네임");
        assertThat(response.partnerNickname()).isEqualTo("상대닉네임");
        assertThat(response.myProfile()).isEqualTo(CompromiseProfileResponse.from(myProfile));
        assertThat(response.partnerProfile()).isEqualTo(CompromiseProfileResponse.from(partnerProfile));
        assertThat(response.compromiseProfile()).isEqualTo(CompromiseProfileResponse.from(compromise));

        List<RecommendedCocktailResponse> recommendations = response.recommendations();
        assertThat(recommendations).hasSize(3);
        assertThat(recommendations)
                .extracting(RecommendedCocktailResponse::cocktailId)
                .containsExactly(101L, 102L, 103L);
        assertThat(recommendations)
                .extracting(RecommendedCocktailResponse::ranking)
                .containsExactly(1, 2, 3);
        assertThat(recommendations.get(0).matchScore()).isEqualTo(100);

        assertThat(response.tasteContributions()).hasSize(2);
    }

    @Test
    void throwsMoodTestNotFoundWhenMyLatestResultDoesNotExist() {
        when(moodTestResultRepository.findFirstByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.recommendPair(1L, "MOOD-4821"))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode())
                                .isEqualTo(MoodTestErrorStatus.MOOD_TEST_RESULT_NOT_FOUND.getCode().getCode())
                );
        verifyNoInteractions(inviteCodeService);
    }

    @Test
    void throwsInviteCodeNotFoundWhenPartnerInviteCodeDoesNotExist() {
        User me = userWithId(1L, "나닉네임");
        MoodTestResult myResult = latestResultOf(me, 10L, TasteProfile.of(
                new BigDecimal("2.0"), new BigDecimal("2.0"), new BigDecimal("2.0"),
                new BigDecimal("2.0"), new BigDecimal("2.0")
        ));

        when(moodTestResultRepository.findFirstByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.of(myResult));
        when(inviteCodeService.findUserByInviteCode("INVALID-CODE"))
                .thenThrow(new RestApiException(UserErrorStatus.INVITE_CODE_NOT_FOUND));

        assertThatThrownBy(() -> service.recommendPair(1L, "INVALID-CODE"))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode())
                                .isEqualTo(UserErrorStatus.INVITE_CODE_NOT_FOUND.getCode().getCode())
                );
    }

    @Test
    void throwsMoodTestNotFoundWhenPartnerLatestResultDoesNotExist() {
        User me = userWithId(1L, "나닉네임");
        User partner = userWithId(2L, "상대닉네임");
        MoodTestResult myResult = latestResultOf(me, 10L, TasteProfile.of(
                new BigDecimal("2.0"), new BigDecimal("2.0"), new BigDecimal("2.0"),
                new BigDecimal("2.0"), new BigDecimal("2.0")
        ));

        when(moodTestResultRepository.findFirstByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.of(myResult));
        when(inviteCodeService.findUserByInviteCode("MOOD-4821")).thenReturn(partner);
        when(moodTestResultRepository.findFirstByUserIdOrderByCreatedAtDesc(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.recommendPair(1L, "MOOD-4821"))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode())
                                .isEqualTo(MoodTestErrorStatus.MOOD_TEST_RESULT_NOT_FOUND.getCode().getCode())
                );
    }

    @Test
    void returnsOnlyAvailableCocktailsWhenFewerThanRecommendationLimitExist() {
        User me = userWithId(1L, "나닉네임");
        User partner = userWithId(2L, "상대닉네임");
        TasteProfile profile = TasteProfile.of(
                new BigDecimal("3.0"), new BigDecimal("3.0"), new BigDecimal("3.0"),
                new BigDecimal("3.0"), new BigDecimal("3.0")
        );
        MoodTestResult myResult = latestResultOf(me, 10L, profile);
        MoodTestResult partnerResult = latestResultOf(partner, 20L, profile);

        when(moodTestResultRepository.findFirstByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.of(myResult));
        when(inviteCodeService.findUserByInviteCode("MOOD-4821")).thenReturn(partner);
        when(moodTestResultRepository.findFirstByUserIdOrderByCreatedAtDesc(2L)).thenReturn(Optional.of(partnerResult));
        when(cocktailRepository.findAll()).thenReturn(List.of(
                cocktail(301L, "A", "A", new BigDecimal("3.0"), new BigDecimal("3.0"),
                        new BigDecimal("3.0"), new BigDecimal("3.0"), new BigDecimal("3.0")),
                cocktail(302L, "B", "B", new BigDecimal("3.5"), new BigDecimal("3.0"),
                        new BigDecimal("3.0"), new BigDecimal("3.0"), new BigDecimal("3.0"))
        ));

        PairRecommendationResponse response = service.recommendPair(1L, "MOOD-4821");

        assertThat(response.recommendations()).hasSize(2);
        assertThat(response.recommendations())
                .extracting(RecommendedCocktailResponse::ranking)
                .containsExactly(1, 2);
    }

    private User userWithId(long id, String nickname) {
        User user = User.createGuest("guest-" + id, nickname, LocalDateTime.now());
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private MoodTestResult latestResultOf(User owner, long id, TasteProfile profile) {
        MoodTestResult result = MoodTestResult.create(owner, null, LocalDate.now(), profile);
        ReflectionTestUtils.setField(result, "id", id);
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
