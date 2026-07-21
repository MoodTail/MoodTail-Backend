package com.example.moodtail.domain.cocktail.service;

import com.example.moodtail.domain.cocktail.dto.response.CocktailTrendResponse;
import com.example.moodtail.domain.cocktail.enums.TrendPeriod;
import com.example.moodtail.domain.moodtest.repository.MoodTestResultRepository;
import com.example.moodtail.domain.recommendation.entity.RecommendationSessionType;
import com.example.moodtail.domain.recommendation.repository.RecommendationItemRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.*;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.moodtail.global.common.exception.code.status.CocktailErrorStatus.INVALID_TREND_PERIOD;

@Service
@RequiredArgsConstructor
public class CocktailTrendService {

    private static final int POPULAR_MOOD_TYPE_LIMIT = 3;
    private static final int POPULAR_COCKTAIL_LIMIT = 5;
    private static final int TASTE_PROFILE_SCALE = 2;
    private static final BigDecimal DISPLAY_SCORE_MULTIPLIER = BigDecimal.valueOf(20);
    private static final BigDecimal RATIO_MULTIPLIER = BigDecimal.valueOf(100);

    private final MoodTestResultRepository moodTestResultRepository;
    private final RecommendationItemRepository recommendationItemRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public CocktailTrendResponse getTrend(String periodValue, Long moodTypeId) {
        TrendPeriod period = parsePeriod(periodValue);
        LocalDate today = LocalDate.now(clock);
        DateRange currentRange = currentRange(period, today);
        DateRange previousRange = previousRange(period, today);

        List<CocktailTrendResponse.PopularMoodType> popularMoodTypes = buildPopularMoodTypes(currentRange, previousRange);
        CocktailTrendResponse.TasteProfile averageTasteProfile = buildAverageTasteProfile(currentRange);
        CocktailTrendResponse.DisplayTasteScores displayAverageTasteScores = toDisplayScores(averageTasteProfile);

        List<CocktailTrendResponse.PopularCocktail> popularCocktails = buildPopularCocktails(currentRange, previousRange);
        List<CocktailTrendResponse.RankChangeCocktail> rankChangeCocktails = buildRankChangeCocktails(popularCocktails);

        List<CocktailTrendResponse.SameTypePopularCocktail> sameTypePopularCocktails = moodTypeId == null
                ? null
                : buildSameTypePopularCocktails(currentRange, moodTypeId);

        return new CocktailTrendResponse(
                period.name(),
                popularMoodTypes,
                averageTasteProfile,
                displayAverageTasteScores,
                popularCocktails,
                rankChangeCocktails,
                sameTypePopularCocktails
        );
    }

    private TrendPeriod parsePeriod(String periodValue) {
        if (periodValue == null || periodValue.isBlank()) {
            return TrendPeriod.WEEKLY;
        }
        try {
            return TrendPeriod.valueOf(periodValue);
        } catch (IllegalArgumentException exception) {
            throw new RestApiException(INVALID_TREND_PERIOD);
        }
    }

    private DateRange currentRange(TrendPeriod period, LocalDate today) {
        return switch (period) {
            case DAILY -> new DateRange(today, today);
            case WEEKLY -> new DateRange(today.minusDays(6), today);
            case MONTHLY -> new DateRange(today.minusDays(29), today);
        };
    }

    private DateRange previousRange(TrendPeriod period, LocalDate today) {
        return switch (period) {
            case DAILY -> new DateRange(today.minusDays(1), today.minusDays(1));
            case WEEKLY -> new DateRange(today.minusDays(13), today.minusDays(7));
            case MONTHLY -> new DateRange(today.minusDays(59), today.minusDays(30));
        };
    }

    private List<CocktailTrendResponse.PopularMoodType> buildPopularMoodTypes(
            DateRange currentRange,
            DateRange previousRange
    ) {
        List<MoodTestResultRepository.MoodTypeTrendCount> currentCounts =
                moodTestResultRepository.countMoodTypesByResultDateBetween(currentRange.startDate(), currentRange.endDate());
        List<MoodTestResultRepository.MoodTypeTrendCount> previousCounts =
                moodTestResultRepository.countMoodTypesByResultDateBetween(previousRange.startDate(), previousRange.endDate());

        long total = currentCounts.stream()
                .mapToLong(MoodTestResultRepository.MoodTypeTrendCount::getResultCount)
                .sum();
        Map<Long, Integer> previousRanks = assignRanks(previousCounts, MoodTestResultRepository.MoodTypeTrendCount::getResultCount)
                .stream()
                .collect(Collectors.toMap(ranked -> ranked.item().getMoodTypeId(), RankedItem::rank));

        List<RankedItem<MoodTestResultRepository.MoodTypeTrendCount>> rankedCurrent =
                assignRanks(currentCounts, MoodTestResultRepository.MoodTypeTrendCount::getResultCount);

        List<CocktailTrendResponse.PopularMoodType> popularMoodTypes = new ArrayList<>();
        for (int index = 0; index < Math.min(rankedCurrent.size(), POPULAR_MOOD_TYPE_LIMIT); index++) {
            RankedItem<MoodTestResultRepository.MoodTypeTrendCount> ranked = rankedCurrent.get(index);
            MoodTestResultRepository.MoodTypeTrendCount count = ranked.item();
            Integer previousRank = previousRanks.get(count.getMoodTypeId());
            popularMoodTypes.add(new CocktailTrendResponse.PopularMoodType(
                    ranked.rank(),
                    count.getMoodTypeId(),
                    count.getTypeCode(),
                    count.getName(),
                    count.getResultCount(),
                    calculateRatio(count.getResultCount(), total),
                    previousRank == null ? null : previousRank - ranked.rank()
            ));
        }
        return popularMoodTypes;
    }

    private CocktailTrendResponse.TasteProfile buildAverageTasteProfile(DateRange range) {
        MoodTestResultRepository.AverageTasteProfile average =
                moodTestResultRepository.averageTasteProfileByResultDateBetween(range.startDate(), range.endDate());
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

    private List<CocktailTrendResponse.PopularCocktail> buildPopularCocktails(
            DateRange currentRange,
            DateRange previousRange
    ) {
        List<RecommendationItemRepository.PopularCocktailCount> currentCounts = recommendationItemRepository.countPopularCocktails(
                RecommendationSessionType.TEST_RESULT, currentRange.startDate(), currentRange.endDate(), null
        );
        List<RecommendationItemRepository.PopularCocktailCount> previousCounts = recommendationItemRepository.countPopularCocktails(
                RecommendationSessionType.TEST_RESULT, previousRange.startDate(), previousRange.endDate(), null
        );

        long total = currentCounts.stream()
                .mapToLong(RecommendationItemRepository.PopularCocktailCount::getRecordCount)
                .sum();
        Map<Long, Integer> previousRanks = assignRanks(previousCounts, RecommendationItemRepository.PopularCocktailCount::getRecordCount)
                .stream()
                .collect(Collectors.toMap(ranked -> ranked.item().getCocktailId(), RankedItem::rank));

        List<RankedItem<RecommendationItemRepository.PopularCocktailCount>> rankedCurrent =
                assignRanks(currentCounts, RecommendationItemRepository.PopularCocktailCount::getRecordCount);

        List<CocktailTrendResponse.PopularCocktail> popularCocktails = new ArrayList<>();
        for (int index = 0; index < Math.min(rankedCurrent.size(), POPULAR_COCKTAIL_LIMIT); index++) {
            RankedItem<RecommendationItemRepository.PopularCocktailCount> ranked = rankedCurrent.get(index);
            RecommendationItemRepository.PopularCocktailCount count = ranked.item();
            Integer previousRank = previousRanks.get(count.getCocktailId());
            popularCocktails.add(new CocktailTrendResponse.PopularCocktail(
                    ranked.rank(),
                    count.getCocktailId(),
                    count.getNameKo(),
                    count.getNameEn(),
                    count.getShortDescription(),
                    calculateRatio(count.getRecordCount(), total),
                    count.getRecordCount(),
                    previousRank == null ? null : previousRank - ranked.rank()
            ));
        }
        return popularCocktails;
    }

    private List<CocktailTrendResponse.RankChangeCocktail> buildRankChangeCocktails(
            List<CocktailTrendResponse.PopularCocktail> popularCocktails
    ) {
        CocktailTrendResponse.RankChangeCocktail topRise = popularCocktails.stream()
                .filter(cocktail -> cocktail.rankChange() != null && cocktail.rankChange() > 0)
                .max(Comparator.comparingInt(CocktailTrendResponse.PopularCocktail::rankChange))
                .map(cocktail -> toRankChangeCocktail(cocktail, CocktailTrendResponse.ChangeDirection.UP))
                .orElse(null);

        CocktailTrendResponse.RankChangeCocktail topFall = popularCocktails.stream()
                .filter(cocktail -> cocktail.rankChange() != null && cocktail.rankChange() < 0)
                .min(Comparator.comparingInt(CocktailTrendResponse.PopularCocktail::rankChange))
                .map(cocktail -> toRankChangeCocktail(cocktail, CocktailTrendResponse.ChangeDirection.DOWN))
                .orElse(null);

        return Stream.of(topRise, topFall)
                .filter(Objects::nonNull)
                .toList();
    }

    private CocktailTrendResponse.RankChangeCocktail toRankChangeCocktail(
            CocktailTrendResponse.PopularCocktail cocktail,
            CocktailTrendResponse.ChangeDirection direction
    ) {
        return new CocktailTrendResponse.RankChangeCocktail(
                cocktail.cocktailId(),
                cocktail.nameKo(),
                cocktail.nameEn(),
                Math.abs(cocktail.rankChange()),
                direction
        );
    }

    private List<CocktailTrendResponse.SameTypePopularCocktail> buildSameTypePopularCocktails(
            DateRange currentRange,
            Long moodTypeId
    ) {
        List<RecommendationItemRepository.PopularCocktailCount> counts = recommendationItemRepository.countPopularCocktails(
                RecommendationSessionType.TEST_RESULT, currentRange.startDate(), currentRange.endDate(), moodTypeId
        );
        List<RankedItem<RecommendationItemRepository.PopularCocktailCount>> ranked =
                assignRanks(counts, RecommendationItemRepository.PopularCocktailCount::getRecordCount);

        List<CocktailTrendResponse.SameTypePopularCocktail> result = new ArrayList<>();
        for (int index = 0; index < Math.min(ranked.size(), POPULAR_COCKTAIL_LIMIT); index++) {
            RankedItem<RecommendationItemRepository.PopularCocktailCount> item = ranked.get(index);
            result.add(new CocktailTrendResponse.SameTypePopularCocktail(
                    item.rank(),
                    item.item().getCocktailId(),
                    item.item().getNameKo(),
                    item.item().getRecordCount()
            ));
        }
        return result;
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
}
