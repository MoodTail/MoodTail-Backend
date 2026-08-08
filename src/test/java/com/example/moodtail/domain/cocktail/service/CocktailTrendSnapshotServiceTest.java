package com.example.moodtail.domain.cocktail.service;

import com.example.moodtail.domain.cocktail.dto.response.CocktailTrendResponse;
import com.example.moodtail.domain.cocktail.entity.CocktailTrendSnapshot;
import com.example.moodtail.domain.cocktail.repository.CocktailTrendSnapshotRepository;
import com.example.moodtail.domain.moodtest.repository.MoodTestResultRepository;
import com.example.moodtail.domain.recommendation.entity.RecommendationSessionType;
import com.example.moodtail.domain.recommendation.repository.RecommendationItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CocktailTrendSnapshotServiceTest {

    // 2026-07-22(수) 기준 이번주 월~일: 07-20~07-26, 지난주 월~일: 07-13~07-19
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-07-22T03:30:00Z"),
            ZoneId.of("Asia/Seoul")
    );
    private static final LocalDate CURRENT_WEEK_START = LocalDate.of(2026, 7, 20);
    private static final LocalDate CURRENT_WEEK_END = LocalDate.of(2026, 7, 26);
    private static final LocalDate PREVIOUS_WEEK_START = LocalDate.of(2026, 7, 13);
    private static final LocalDate PREVIOUS_WEEK_END = LocalDate.of(2026, 7, 19);

    @Mock
    private MoodTestResultRepository moodTestResultRepository;
    @Mock
    private RecommendationItemRepository recommendationItemRepository;
    @Mock
    private CocktailTrendSnapshotRepository cocktailTrendSnapshotRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private CocktailTrendSnapshotService cocktailTrendSnapshotService;

    @BeforeEach
    void setUp() {
        cocktailTrendSnapshotService = new CocktailTrendSnapshotService(
                moodTestResultRepository,
                recommendationItemRepository,
                cocktailTrendSnapshotRepository,
                objectMapper,
                CLOCK
        );
    }

    @Test
    void aggregatesWeeklyMoodTypesAndCumulativeCocktailsWithWeeklyRankChange() {
        when(moodTestResultRepository.countMoodTypesByResultDateBetween(CURRENT_WEEK_START, CURRENT_WEEK_END))
                .thenReturn(List.of(
                        moodTypeCount(1L, "FRESH_SPARK", "상큼주의자", 10),
                        moodTypeCount(2L, "CALM_DEPTH", "차분주의자", 6),
                        moodTypeCount(3L, "BOLD_HEAT", "열정주의자", 4),
                        moodTypeCount(4L, "SOFT_MIST", "몽환주의자", 2)
                ));
        when(moodTestResultRepository.averageTasteProfileCumulative())
                .thenReturn(averageTasteProfile("2.20", "3.50", "2.90", "3.70", "1.50"));

        when(recommendationItemRepository.countPopularCocktailsCumulative(RecommendationSessionType.TEST_RESULT))
                .thenReturn(List.of(
                        cocktailCount(7L, "피치 하이볼", "Peach Highball", "달콤하고 청량한 추천", 340),
                        cocktailCount(8L, "선라이즈 소다", "Sunrise Soda", "과일향 중심의 추천", 290),
                        cocktailCount(9L, "모히토", "Mojito", "상쾌한 민트 추천", 240),
                        cocktailCount(15L, "진 토닉", "Gin Tonic", "깔끔한 쓴맛 추천", 190),
                        cocktailCount(20L, "위스키 사워", "Whiskey Sour", "산미 있는 클래식", 140),
                        cocktailCount(99L, "기타 칵테일", "Other Cocktail", "기타", 50)
                ));
        when(recommendationItemRepository.countPopularCocktails(
                RecommendationSessionType.TEST_RESULT, CURRENT_WEEK_START, CURRENT_WEEK_END
        )).thenReturn(List.of(
                cocktailCount(7L, "피치 하이볼", "Peach Highball", "달콤하고 청량한 추천", 34),
                cocktailCount(8L, "선라이즈 소다", "Sunrise Soda", "과일향 중심의 추천", 29),
                cocktailCount(9L, "모히토", "Mojito", "상쾌한 민트 추천", 24),
                cocktailCount(15L, "진 토닉", "Gin Tonic", "깔끔한 쓴맛 추천", 19),
                cocktailCount(20L, "위스키 사워", "Whiskey Sour", "산미 있는 클래식", 14)
        ));
        when(recommendationItemRepository.countPopularCocktails(
                RecommendationSessionType.TEST_RESULT, PREVIOUS_WEEK_START, PREVIOUS_WEEK_END
        )).thenReturn(List.of(
                cocktailCount(8L, "선라이즈 소다", "Sunrise Soda", "과일향 중심의 추천", 50),
                cocktailCount(7L, "피치 하이볼", "Peach Highball", "달콤하고 청량한 추천", 45),
                cocktailCount(15L, "진 토닉", "Gin Tonic", "깔끔한 쓴맛 추천", 40),
                cocktailCount(20L, "위스키 사워", "Whiskey Sour", "산미 있는 클래식", 35),
                cocktailCount(9L, "모히토", "Mojito", "상쾌한 민트 추천", 5)
        ));
        when(cocktailTrendSnapshotRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());

        cocktailTrendSnapshotService.refreshSnapshot();

        CocktailTrendResponse response = capturedSnapshotData();

        assertThat(response.popularMoodTypes()).hasSize(3);
        assertThat(response.popularMoodTypes()).extracting(
                CocktailTrendResponse.PopularMoodType::ranking,
                CocktailTrendResponse.PopularMoodType::moodTypeId,
                CocktailTrendResponse.PopularMoodType::resultCount,
                CocktailTrendResponse.PopularMoodType::ratio
        ).containsExactly(
                org.assertj.core.groups.Tuple.tuple(1, 1L, 10L, 45),
                org.assertj.core.groups.Tuple.tuple(2, 2L, 6L, 27),
                org.assertj.core.groups.Tuple.tuple(3, 3L, 4L, 18)
        );

        assertThat(response.averageTasteProfile().alcoholIntensity()).isEqualByComparingTo("2.20");
        assertThat(response.displayAverageTasteScores().alcoholIntensity()).isEqualTo(44);
        assertThat(response.displayAverageTasteScores().sweetness()).isEqualTo(70);
        assertThat(response.displayAverageTasteScores().bitterness()).isEqualTo(30);

        // ranking/ratio/recordCount는 누적 집계, rankChange는 주간(월~일) 비교값
        assertThat(response.popularCocktails()).hasSize(5);
        assertThat(response.popularCocktails()).extracting(
                CocktailTrendResponse.PopularCocktail::ranking,
                CocktailTrendResponse.PopularCocktail::cocktailId,
                CocktailTrendResponse.PopularCocktail::recordCount,
                CocktailTrendResponse.PopularCocktail::rankChange
        ).containsExactly(
                org.assertj.core.groups.Tuple.tuple(1, 7L, 340L, 1),
                org.assertj.core.groups.Tuple.tuple(2, 8L, 290L, -1),
                org.assertj.core.groups.Tuple.tuple(3, 9L, 240L, 2),
                org.assertj.core.groups.Tuple.tuple(4, 15L, 190L, -1),
                org.assertj.core.groups.Tuple.tuple(5, 20L, 140L, -1)
        );

        assertThat(response.rankChangeCocktails()).hasSize(2);
        assertThat(response.rankChangeCocktails().get(0).cocktailId()).isEqualTo(9L);
        assertThat(response.rankChangeCocktails().get(0).rankChange()).isEqualTo(2);
        assertThat(response.rankChangeCocktails().get(0).changeDirection())
                .isEqualTo(CocktailTrendResponse.ChangeDirection.UP);
        assertThat(response.rankChangeCocktails().get(1).cocktailId()).isEqualTo(8L);
        assertThat(response.rankChangeCocktails().get(1).rankChange()).isEqualTo(1);
        assertThat(response.rankChangeCocktails().get(1).changeDirection())
                .isEqualTo(CocktailTrendResponse.ChangeDirection.DOWN);
    }

    @Test
    void rankChangeIsNullWhenCocktailHadNoRankLastWeek() {
        when(moodTestResultRepository.countMoodTypesByResultDateBetween(CURRENT_WEEK_START, CURRENT_WEEK_END))
                .thenReturn(List.of());
        when(moodTestResultRepository.averageTasteProfileCumulative())
                .thenReturn(averageTasteProfile(null, null, null, null, null));
        when(recommendationItemRepository.countPopularCocktailsCumulative(RecommendationSessionType.TEST_RESULT))
                .thenReturn(List.of(
                        cocktailCount(12L, "선샤인 피즈", "Sunshine Fizz", "설명", 12)
                ));
        when(recommendationItemRepository.countPopularCocktails(
                RecommendationSessionType.TEST_RESULT, CURRENT_WEEK_START, CURRENT_WEEK_END
        )).thenReturn(List.of(
                cocktailCount(12L, "선샤인 피즈", "Sunshine Fizz", "설명", 3)
        ));
        when(recommendationItemRepository.countPopularCocktails(
                RecommendationSessionType.TEST_RESULT, PREVIOUS_WEEK_START, PREVIOUS_WEEK_END
        )).thenReturn(List.of());
        when(cocktailTrendSnapshotRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());

        cocktailTrendSnapshotService.refreshSnapshot();

        CocktailTrendResponse response = capturedSnapshotData();

        assertThat(response.popularCocktails()).hasSize(1);
        assertThat(response.popularCocktails().get(0).cocktailId()).isEqualTo(12L);
        assertThat(response.popularCocktails().get(0).rankChange()).isNull();
        assertThat(response.rankChangeCocktails()).isEmpty();
        assertThat(response.averageTasteProfile().alcoholIntensity()).isEqualByComparingTo("0.00");
        assertThat(response.displayAverageTasteScores().alcoholIntensity()).isEqualTo(0);
    }

    @Test
    void updatesExistingSnapshotInsteadOfCreatingNewOneWhenOneAlreadyExists() {
        when(moodTestResultRepository.countMoodTypesByResultDateBetween(CURRENT_WEEK_START, CURRENT_WEEK_END))
                .thenReturn(List.of());
        when(moodTestResultRepository.averageTasteProfileCumulative())
                .thenReturn(averageTasteProfile(null, null, null, null, null));
        when(recommendationItemRepository.countPopularCocktailsCumulative(RecommendationSessionType.TEST_RESULT))
                .thenReturn(List.of());
        when(recommendationItemRepository.countPopularCocktails(
                RecommendationSessionType.TEST_RESULT, CURRENT_WEEK_START, CURRENT_WEEK_END
        )).thenReturn(List.of());
        when(recommendationItemRepository.countPopularCocktails(
                RecommendationSessionType.TEST_RESULT, PREVIOUS_WEEK_START, PREVIOUS_WEEK_END
        )).thenReturn(List.of());

        CocktailTrendSnapshot existingSnapshot = CocktailTrendSnapshot.create(
                serialize(new CocktailTrendResponse(List.of(), dummyTasteProfile(), dummyDisplayScores(), List.of(), List.of())),
                java.time.LocalDateTime.now(CLOCK).minusMinutes(10)
        );
        when(cocktailTrendSnapshotRepository.findTopByOrderByIdDesc()).thenReturn(Optional.of(existingSnapshot));

        cocktailTrendSnapshotService.refreshSnapshot();

        verify(cocktailTrendSnapshotRepository, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.any());
        assertThat(existingSnapshot.getUpdatedAt()).isEqualTo(java.time.LocalDateTime.now(CLOCK));
    }

    private CocktailTrendResponse capturedSnapshotData() {
        ArgumentCaptor<CocktailTrendSnapshot> captor = ArgumentCaptor.forClass(CocktailTrendSnapshot.class);
        verify(cocktailTrendSnapshotRepository).save(captor.capture());
        return deserialize(captor.getValue().getSnapshotData());
    }

    private String serialize(CocktailTrendResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private CocktailTrendResponse deserialize(String snapshotData) {
        try {
            return objectMapper.readValue(snapshotData, CocktailTrendResponse.class);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private CocktailTrendResponse.TasteProfile dummyTasteProfile() {
        return new CocktailTrendResponse.TasteProfile(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );
    }

    private CocktailTrendResponse.DisplayTasteScores dummyDisplayScores() {
        return new CocktailTrendResponse.DisplayTasteScores(0, 0, 0, 0, 0);
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
