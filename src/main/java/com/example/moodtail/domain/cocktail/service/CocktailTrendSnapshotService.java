package com.example.moodtail.domain.cocktail.service;

import com.example.moodtail.domain.cocktail.dto.response.CocktailTrendResponse;
import com.example.moodtail.domain.cocktail.entity.CocktailTrendSnapshot;
import com.example.moodtail.domain.cocktail.repository.CocktailTrendSnapshotRepository;
import com.example.moodtail.domain.moodtest.repository.MoodTestResultRepository;
import com.example.moodtail.domain.recommendation.entity.RecommendationSessionType;
import com.example.moodtail.domain.recommendation.repository.RecommendationItemRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// 칵테일 트렌드 집계를 계산해서 스냅샷 테이블에 저장하는 책임 (기존 CocktailTrendService에서 옮겨온 계산 로직)
@Service
@RequiredArgsConstructor
public class CocktailTrendSnapshotService {

    private static final int POPULAR_MOOD_TYPE_LIMIT = 3;
    private static final int POPULAR_COCKTAIL_LIMIT = 5;
    private static final int TASTE_PROFILE_SCALE = 2;
    private static final BigDecimal DISPLAY_SCORE_MULTIPLIER = BigDecimal.valueOf(20);
    private static final BigDecimal RATIO_MULTIPLIER = BigDecimal.valueOf(100);

    private final MoodTestResultRepository moodTestResultRepository;
    private final RecommendationItemRepository recommendationItemRepository;
    private final CocktailTrendSnapshotRepository cocktailTrendSnapshotRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    // 트렌드를 다시 계산해서 스냅샷 테이블에 저장(있으면 갱신, 없으면 신규 저장)
    @Transactional
    public void refreshSnapshot() {
        String snapshotData = serialize(calculateTrend());
        LocalDateTime updatedAt = LocalDateTime.now(clock);

        cocktailTrendSnapshotRepository.findTopByOrderByIdDesc()
                .ifPresentOrElse(
                        snapshot -> snapshot.updateSnapshot(snapshotData, updatedAt),
                        () -> cocktailTrendSnapshotRepository.save(CocktailTrendSnapshot.create(snapshotData, updatedAt))
                );
    }

    private String serialize(CocktailTrendResponse trend) {
        try {
            return objectMapper.writeValueAsString(trend);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("칵테일 트렌드 스냅샷 직렬화에 실패했습니다.", e);
        }
    }

    private CocktailTrendResponse calculateTrend() {
        LocalDate today = LocalDate.now(clock);
        DateRange currentWeek = currentWeekRange(today);
        DateRange previousWeek = previousWeekRange(today);

        List<CocktailTrendResponse.PopularMoodType> popularMoodTypes = buildPopularMoodTypes(currentWeek);
        CocktailTrendResponse.TasteProfile averageTasteProfile = buildAverageTasteProfile();
        CocktailTrendResponse.DisplayTasteScores displayAverageTasteScores = toDisplayScores(averageTasteProfile);

        List<WeeklyCocktailRankChange> weeklyRankChanges = buildWeeklyCocktailRankChanges(currentWeek, previousWeek);
        List<CocktailTrendResponse.PopularCocktail> popularCocktails = buildPopularCocktails(weeklyRankChanges);
        List<CocktailTrendResponse.RankChangeCocktail> rankChangeCocktails = buildRankChangeCocktails(weeklyRankChanges);

        return new CocktailTrendResponse(
                popularMoodTypes,
                averageTasteProfile,
                displayAverageTasteScores,
                popularCocktails,
                rankChangeCocktails
        );
    }

    private DateRange currentWeekRange(LocalDate today) {
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        return new DateRange(monday, sunday);
    }

    private DateRange previousWeekRange(LocalDate today) {
        DateRange currentWeek = currentWeekRange(today);
        return new DateRange(currentWeek.startDate().minusWeeks(1), currentWeek.endDate().minusWeeks(1));
    }

    private List<CocktailTrendResponse.PopularMoodType> buildPopularMoodTypes(DateRange currentWeek) {
        List<MoodTestResultRepository.MoodTypeTrendCount> currentCounts =
                moodTestResultRepository.countMoodTypesByResultDateBetween(currentWeek.startDate(), currentWeek.endDate());

        long total = currentCounts.stream()
                .mapToLong(MoodTestResultRepository.MoodTypeTrendCount::getResultCount)
                .sum();

        List<RankedItem<MoodTestResultRepository.MoodTypeTrendCount>> rankedCurrent =
                assignRanks(currentCounts, MoodTestResultRepository.MoodTypeTrendCount::getResultCount);

        List<CocktailTrendResponse.PopularMoodType> popularMoodTypes = new ArrayList<>();
        for (int index = 0; index < Math.min(rankedCurrent.size(), POPULAR_MOOD_TYPE_LIMIT); index++) {
            RankedItem<MoodTestResultRepository.MoodTypeTrendCount> ranked = rankedCurrent.get(index);
            MoodTestResultRepository.MoodTypeTrendCount count = ranked.item();
            popularMoodTypes.add(new CocktailTrendResponse.PopularMoodType(
                    ranked.rank(),
                    count.getMoodTypeId(),
                    count.getTypeCode(),
                    count.getName(),
                    count.getResultCount(),
                    calculateRatio(count.getResultCount(), total)
            ));
        }
        return popularMoodTypes;
    }

    private CocktailTrendResponse.TasteProfile buildAverageTasteProfile() {
        MoodTestResultRepository.AverageTasteProfile average =
                moodTestResultRepository.averageTasteProfileCumulative();
        return new CocktailTrendResponse.TasteProfile(
                scaled(average.getAlcoholIntensity()),
                scaled(average.getSweetness()),
                scaled(average.getSourness()),
                scaled(average.getRefreshing()),
                scaled(average.getBitterness())
        );
    }

    private BigDecimal scaled(BigDecimal value) {
        BigDecimal base = value == null ? BigDecimal.ZERO : value;
        return base.setScale(TASTE_PROFILE_SCALE, RoundingMode.HALF_UP);
    }

    private CocktailTrendResponse.DisplayTasteScores toDisplayScores(CocktailTrendResponse.TasteProfile profile) {
        return new CocktailTrendResponse.DisplayTasteScores(
                toDisplayScore(profile.alcoholIntensity()),
                toDisplayScore(profile.sweetness()),
                toDisplayScore(profile.sourness()),
                toDisplayScore(profile.refreshing()),
                toDisplayScore(profile.bitterness())
        );
    }

    private int toDisplayScore(BigDecimal tasteScore) {
        int value = tasteScore.multiply(DISPLAY_SCORE_MULTIPLIER)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
        return Math.max(0, Math.min(100, value));
    }

    private List<WeeklyCocktailRankChange> buildWeeklyCocktailRankChanges(DateRange currentWeek, DateRange previousWeek) {
        List<RecommendationItemRepository.PopularCocktailCount> currentCounts = recommendationItemRepository.countPopularCocktails(
                RecommendationSessionType.TEST_RESULT, currentWeek.startDate(), currentWeek.endDate()
        );
        List<RecommendationItemRepository.PopularCocktailCount> previousCounts = recommendationItemRepository.countPopularCocktails(
                RecommendationSessionType.TEST_RESULT, previousWeek.startDate(), previousWeek.endDate()
        );

        Map<Long, Integer> previousRanks = assignRanks(previousCounts, RecommendationItemRepository.PopularCocktailCount::getRecordCount)
                .stream()
                .collect(Collectors.toMap(ranked -> ranked.item().getCocktailId(), RankedItem::rank));

        List<RankedItem<RecommendationItemRepository.PopularCocktailCount>> rankedCurrent =
                assignRanks(currentCounts, RecommendationItemRepository.PopularCocktailCount::getRecordCount);

        List<WeeklyCocktailRankChange> weeklyRankChanges = new ArrayList<>();
        for (RankedItem<RecommendationItemRepository.PopularCocktailCount> ranked : rankedCurrent) {
            RecommendationItemRepository.PopularCocktailCount count = ranked.item();
            Integer previousRank = previousRanks.get(count.getCocktailId());
            Integer rankChange = previousRank == null ? null : previousRank - ranked.rank();
            weeklyRankChanges.add(new WeeklyCocktailRankChange(count.getCocktailId(), count.getNameKo(), count.getNameEn(), rankChange));
        }
        return weeklyRankChanges;
    }

    private List<CocktailTrendResponse.PopularCocktail> buildPopularCocktails(List<WeeklyCocktailRankChange> weeklyRankChanges) {
        List<RecommendationItemRepository.PopularCocktailCount> cumulativeCounts =
                recommendationItemRepository.countPopularCocktailsCumulative(RecommendationSessionType.TEST_RESULT);

        Map<Long, Integer> rankChangeByCocktailId = new HashMap<>();
        for (WeeklyCocktailRankChange item : weeklyRankChanges) {
            rankChangeByCocktailId.put(item.cocktailId(), item.rankChange());
        }

        long total = cumulativeCounts.stream()
                .mapToLong(RecommendationItemRepository.PopularCocktailCount::getRecordCount)
                .sum();

        List<RankedItem<RecommendationItemRepository.PopularCocktailCount>> rankedCurrent =
                assignRanks(cumulativeCounts, RecommendationItemRepository.PopularCocktailCount::getRecordCount);

        List<CocktailTrendResponse.PopularCocktail> popularCocktails = new ArrayList<>();
        for (int index = 0; index < Math.min(rankedCurrent.size(), POPULAR_COCKTAIL_LIMIT); index++) {
            RankedItem<RecommendationItemRepository.PopularCocktailCount> ranked = rankedCurrent.get(index);
            RecommendationItemRepository.PopularCocktailCount count = ranked.item();
            popularCocktails.add(new CocktailTrendResponse.PopularCocktail(
                    ranked.rank(),
                    count.getCocktailId(),
                    count.getNameKo(),
                    count.getNameEn(),
                    count.getShortDescription(),
                    calculateRatio(count.getRecordCount(), total),
                    count.getRecordCount(),
                    rankChangeByCocktailId.get(count.getCocktailId())
            ));
        }
        return popularCocktails;
    }

    private List<CocktailTrendResponse.RankChangeCocktail> buildRankChangeCocktails(List<WeeklyCocktailRankChange> weeklyRankChanges) {
        CocktailTrendResponse.RankChangeCocktail topRise = weeklyRankChanges.stream()
                .filter(item -> item.rankChange() != null && item.rankChange() > 0)
                .max(Comparator.comparingInt(WeeklyCocktailRankChange::rankChange))
                .map(item -> toRankChangeCocktail(item, CocktailTrendResponse.ChangeDirection.UP))
                .orElse(null);

        CocktailTrendResponse.RankChangeCocktail topFall = weeklyRankChanges.stream()
                .filter(item -> item.rankChange() != null && item.rankChange() < 0)
                .min(Comparator.comparingInt(WeeklyCocktailRankChange::rankChange))
                .map(item -> toRankChangeCocktail(item, CocktailTrendResponse.ChangeDirection.DOWN))
                .orElse(null);

        return Stream.of(topRise, topFall)
                .filter(Objects::nonNull)
                .toList();
    }

    private CocktailTrendResponse.RankChangeCocktail toRankChangeCocktail(
            WeeklyCocktailRankChange item,
            CocktailTrendResponse.ChangeDirection direction
    ) {
        return new CocktailTrendResponse.RankChangeCocktail(
                item.cocktailId(),
                item.nameKo(),
                item.nameEn(),
                Math.abs(item.rankChange()),
                direction
        );
    }

    private int calculateRatio(long count, long total) {
        if (total == 0) {
            return 0;
        }
        return BigDecimal.valueOf(count)
                .multiply(RATIO_MULTIPLIER)
                .divide(BigDecimal.valueOf(total), 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private <T> List<RankedItem<T>> assignRanks(List<T> sortedDescByCount, ToLongFunction<T> countExtractor) {
        List<RankedItem<T>> ranked = new ArrayList<>(sortedDescByCount.size());
        long previousCount = -1;
        int rank = 0;
        for (int index = 0; index < sortedDescByCount.size(); index++) {
            T item = sortedDescByCount.get(index);
            long count = countExtractor.applyAsLong(item);
            if (count != previousCount) {
                rank = index + 1;
                previousCount = count;
            }
            ranked.add(new RankedItem<>(item, rank));
        }
        return ranked;
    }

    private record DateRange(LocalDate startDate, LocalDate endDate) {
    }

    private record RankedItem<T>(T item, int rank) {
    }

    private record WeeklyCocktailRankChange(Long cocktailId, String nameKo, String nameEn, Integer rankChange) {
    }
}
