package com.example.moodtail.domain.cocktail.service;

import com.example.moodtail.domain.cocktail.dto.response.CocktailTrendResponse;
import com.example.moodtail.domain.moodtest.repository.MoodTestResultRepository;
import com.example.moodtail.domain.recommendation.entity.RecommendationSessionType;
import com.example.moodtail.domain.recommendation.repository.RecommendationItemRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CocktailTrendServiceTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-07-22T03:30:00Z"),
            ZoneId.of("Asia/Seoul")
    );
    private static final LocalDate CURRENT_START = LocalDate.of(2026, 7, 16);
    private static final LocalDate CURRENT_END = LocalDate.of(2026, 7, 22);
    private static final LocalDate PREVIOUS_START = LocalDate.of(2026, 7, 9);
    private static final LocalDate PREVIOUS_END = LocalDate.of(2026, 7, 15);

    @Mock
    private MoodTestResultRepository moodTestResultRepository;
    @Mock
    private RecommendationItemRepository recommendationItemRepository;

    private CocktailTrendService cocktailTrendService;

    @BeforeEach
    void setUp() {
        cocktailTrendService = new CocktailTrendService(
                moodTestResultRepository,
                recommendationItemRepository,
                CLOCK
        );
    }

    @Test
    void aggregatesWeeklyTrendWithRankChangesAgainstPreviousPeriod() {
        when(moodTestResultRepository.countMoodTypesByResultDateBetween(CURRENT_START, CURRENT_END))
                .thenReturn(List.of(
                        moodTypeCount(1L, "FRESH_SPARK", "상큼주의자", 10),
                        moodTypeCount(2L, "CALM_DEPTH", "차분주의자", 6),
                        moodTypeCount(3L, "BOLD_HEAT", "열정주의자", 4),
                        moodTypeCount(4L, "SOFT_MIST", "몽환주의자", 2)
                ));
        when(moodTestResultRepository.countMoodTypesByResultDateBetween(PREVIOUS_START, PREVIOUS_END))
                .thenReturn(List.of(
                        moodTypeCount(2L, "CALM_DEPTH", "차분주의자", 8),
                        moodTypeCount(1L, "FRESH_SPARK", "상큼주의자", 5),
                        moodTypeCount(3L, "BOLD_HEAT", "열정주의자", 3)
                ));
        when(moodTestResultRepository.averageTasteProfileByResultDateBetween(CURRENT_START, CURRENT_END))
                .thenReturn(averageTasteProfile("2.20", "3.50", "2.90", "3.70", "1.50"));

        when(recommendationItemRepository.countPopularCocktails(
                RecommendationSessionType.TEST_RESULT, CURRENT_START, CURRENT_END, null
        )).thenReturn(List.of(
                cocktailCount(7L, "피치 하이볼", "Peach Highball", "달콤하고 청량한 추천", 34),
                cocktailCount(8L, "선라이즈 소다", "Sunrise Soda", "과일향 중심의 추천", 29),
                cocktailCount(9L, "모히토", "Mojito", "상쾌한 민트 추천", 24),
                cocktailCount(15L, "진 토닉", "Gin Tonic", "깔끔한 쓴맛 추천", 19),
                cocktailCount(20L, "위스키 사워", "Whiskey Sour", "산미 있는 클래식", 14),
                cocktailCount(99L, "기타 칵테일", "Other Cocktail", "기타", 5)
        ));
        when(recommendationItemRepository.countPopularCocktails(
                RecommendationSessionType.TEST_RESULT, PREVIOUS_START, PREVIOUS_END, null
        )).thenReturn(List.of(
                cocktailCount(8L, "선라이즈 소다", "Sunrise Soda", "과일향 중심의 추천", 50),
                cocktailCount(7L, "피치 하이볼", "Peach Highball", "달콤하고 청량한 추천", 45),
                cocktailCount(15L, "진 토닉", "Gin Tonic", "깔끔한 쓴맛 추천", 40),
                cocktailCount(20L, "위스키 사워", "Whiskey Sour", "산미 있는 클래식", 35),
                cocktailCount(9L, "모히토", "Mojito", "상쾌한 민트 추천", 5)
        ));

        CocktailTrendResponse response = cocktailTrendService.getTrend(null, null);

        assertThat(response.period()).isEqualTo("WEEKLY");

        assertThat(response.popularMoodTypes()).hasSize(3);
        assertThat(response.popularMoodTypes()).extracting(
                CocktailTrendResponse.PopularMoodType::ranking,
                CocktailTrendResponse.PopularMoodType::moodTypeId,
                CocktailTrendResponse.PopularMoodType::resultCount,
                CocktailTrendResponse.PopularMoodType::ratio,
                CocktailTrendResponse.PopularMoodType::rankChange
        ).containsExactly(
                org.assertj.core.groups.Tuple.tuple(1, 1L, 10L, 45, 1),
                org.assertj.core.groups.Tuple.tuple(2, 2L, 6L, 27, -1),
                org.assertj.core.groups.Tuple.tuple(3, 3L, 4L, 18, 0)
        );

        assertThat(response.averageTasteProfile().alcoholIntensity()).isEqualByComparingTo("2.20");
        assertThat(response.displayAverageTasteScores().alcoholIntensity()).isEqualTo(44);
        assertThat(response.displayAverageTasteScores().sweetness()).isEqualTo(70);
        assertThat(response.displayAverageTasteScores().bitterness()).isEqualTo(30);

        assertThat(response.popularCocktails()).hasSize(5);
        assertThat(response.popularCocktails()).extracting(
                CocktailTrendResponse.PopularCocktail::ranking,
                CocktailTrendResponse.PopularCocktail::cocktailId,
                CocktailTrendResponse.PopularCocktail::ratio,
                CocktailTrendResponse.PopularCocktail::recordCount,
                CocktailTrendResponse.PopularCocktail::rankChange
        ).containsExactly(
                org.assertj.core.groups.Tuple.tuple(1, 7L, 27, 34L, 1),
                org.assertj.core.groups.Tuple.tuple(2, 8L, 23, 29L, -1),
                org.assertj.core.groups.Tuple.tuple(3, 9L, 19, 24L, 2),
                org.assertj.core.groups.Tuple.tuple(4, 15L, 15, 19L, -1),
                org.assertj.core.groups.Tuple.tuple(5, 20L, 11, 14L, -1)
        );

        assertThat(response.rankChangeCocktails()).hasSize(2);
        assertThat(response.rankChangeCocktails().get(0).cocktailId()).isEqualTo(9L);
        assertThat(response.rankChangeCocktails().get(0).rankChange()).isEqualTo(2);
        assertThat(response.rankChangeCocktails().get(0).changeDirection())
                .isEqualTo(CocktailTrendResponse.ChangeDirection.UP);
        assertThat(response.rankChangeCocktails().get(1).cocktailId()).isEqualTo(7L);
        assertThat(response.rankChangeCocktails().get(1).rankChange()).isEqualTo(1);
        assertThat(response.rankChangeCocktails().get(1).changeDirection())
                .isEqualTo(CocktailTrendResponse.ChangeDirection.UP);

        assertThat(response.sameTypePopularCocktails()).isNull();
    }

    @Test
    void includesSameTypePopularCocktailsOnlyWhenMoodTypeIdProvided() {
        when(moodTestResultRepository.countMoodTypesByResultDateBetween(CURRENT_START, CURRENT_END))
                .thenReturn(List.of());
        when(moodTestResultRepository.countMoodTypesByResultDateBetween(PREVIOUS_START, PREVIOUS_END))
                .thenReturn(List.of());
        when(moodTestResultRepository.averageTasteProfileByResultDateBetween(CURRENT_START, CURRENT_END))
                .thenReturn(averageTasteProfile(null, null, null, null, null));
        when(recommendationItemRepository.countPopularCocktails(
                RecommendationSessionType.TEST_RESULT, CURRENT_START, CURRENT_END, null
        )).thenReturn(List.of());
        when(recommendationItemRepository.countPopularCocktails(
                RecommendationSessionType.TEST_RESULT, PREVIOUS_START, PREVIOUS_END, null
        )).thenReturn(List.of());
        when(recommendationItemRepository.countPopularCocktails(
                eq(RecommendationSessionType.TEST_RESULT), eq(CURRENT_START), eq(CURRENT_END), eq(3L)
        )).thenReturn(List.of(
                cocktailCount(12L, "선샤인 피즈", "Sunshine Fizz", "설명", 12)
        ));

        CocktailTrendResponse response = cocktailTrendService.getTrend("WEEKLY", 3L);

        assertThat(response.sameTypePopularCocktails()).hasSize(1);
        assertThat(response.sameTypePopularCocktails().get(0).ranking()).isEqualTo(1);
        assertThat(response.sameTypePopularCocktails().get(0).cocktailId()).isEqualTo(12L);
        assertThat(response.sameTypePopularCocktails().get(0).recordCount()).isEqualTo(12L);
        assertThat(response.averageTasteProfile().alcoholIntensity()).isEqualByComparingTo("0.00");
        assertThat(response.displayAverageTasteScores().alcoholIntensity()).isEqualTo(0);
    }

    @Test
    void throwsRestApiExceptionWhenPeriodIsInvalid() {
        assertThatThrownBy(() -> cocktailTrendService.getTrend("YEARLY", null))
                .isInstanceOf(RestApiException.class)
                .satisfies(exception ->
                        assertThat(((RestApiException) exception).getErrorCode().getCode()).isEqualTo("TREND_400"));
    }

    private MoodTestResultRepository.MoodTypeTrendCount moodTypeCount(
            Long moodTypeId, String typeCode, String name, long resultCount
    ) {
        return new MoodTestResultRepository.MoodTypeTrendCount() {
            @Override
            public Long getMoodTypeId() {
                return moodTypeId;
            }

            @Override
            public String getTypeCode() {
                return typeCode;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public long getResultCount() {
                return resultCount;
            }
        };
    }

    private MoodTestResultRepository.AverageTasteProfile averageTasteProfile(
            String alcoholIntensity, String sweetness, String sourness, String refreshing, String bitterness
    ) {
        return new MoodTestResultRepository.AverageTasteProfile() {
            @Override
            public BigDecimal getAlcoholIntensity() {
                return alcoholIntensity == null ? null : new BigDecimal(alcoholIntensity);
            }

            @Override
            public BigDecimal getSweetness() {
                return sweetness == null ? null : new BigDecimal(sweetness);
            }

            @Override
            public BigDecimal getSourness() {
                return sourness == null ? null : new BigDecimal(sourness);
            }

            @Override
            public BigDecimal getRefreshing() {
                return refreshing == null ? null : new BigDecimal(refreshing);
            }

            @Override
            public BigDecimal getBitterness() {
                return bitterness == null ? null : new BigDecimal(bitterness);
            }
        };
    }

    private RecommendationItemRepository.PopularCocktailCount cocktailCount(
            Long cocktailId, String nameKo, String nameEn, String shortDescription, long recordCount
    ) {
        return new RecommendationItemRepository.PopularCocktailCount() {
            @Override
            public Long getCocktailId() {
                return cocktailId;
            }

            @Override
            public String getNameKo() {
                return nameKo;
            }

            @Override
            public String getNameEn() {
                return nameEn;
            }

            @Override
            public String getShortDescription() {
                return shortDescription;
            }

            @Override
            public long getRecordCount() {
                return recordCount;
            }
        };
    }
}
