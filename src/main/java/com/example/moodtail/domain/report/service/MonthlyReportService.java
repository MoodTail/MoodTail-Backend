package com.example.moodtail.domain.report.service;

import com.example.moodtail.domain.history.repository.HistoryRepository;
import com.example.moodtail.domain.history.repository.HistoryMoodTestResultRepository;
import com.example.moodtail.domain.history.service.HistoryDatePolicy;
import com.example.moodtail.domain.image.entity.Image;
import com.example.moodtail.domain.moodtest.entity.MoodTestResult;
import com.example.moodtail.domain.moodtest.entity.MoodType;
import com.example.moodtail.domain.report.dto.response.MonthlyReportResponse;
import com.example.moodtail.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.example.moodtail.global.common.exception.code.status.ReportErrorStatus.INSUFFICIENT_DATA;
import static com.example.moodtail.global.common.exception.code.status.ReportErrorStatus.INVALID_REQUEST;

@Service
@RequiredArgsConstructor
public class MonthlyReportService {

    private static final int TOP_MOOD_TYPE_LIMIT = 3;
    private static final int TOP_COCKTAIL_LIMIT = 3;
    private static final int REQUIRED_TEST_RESULT_COUNT = 5;
    private static final BigDecimal DISPLAY_SCORE_MULTIPLIER = BigDecimal.valueOf(25);
    private final HistoryMoodTestResultRepository moodTestResultRepository;
    private final HistoryRepository historyRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public MonthlyReportResponse getMonthlyReport(Long userId, int year, int month) {
        LocalDate today = LocalDate.now(clock);
        YearMonth requestedMonth = parseRequestedMonth(year, month);
        if (requestedMonth.isAfter(YearMonth.from(today))) {
            throw new RestApiException(INVALID_REQUEST);
        }

        DateRange currentRange = dateRange(requestedMonth, today);
        List<MoodTestResult> currentResults = moodTestResultRepository.findAllWithMoodType(
                userId,
                currentRange.startDate(),
                currentRange.endDate()
        );
        if (currentResults.size() < REQUIRED_TEST_RESULT_COUNT) {
            throw new RestApiException(INSUFFICIENT_DATA);
        }

        List<MoodTypeCount> moodTypeCounts = countMoodTypes(currentResults);
        MonthlyReportResponse.TasteProfile averageTasteProfile = averageTasteProfile(currentResults);
        MonthlyReportResponse.TasteProfile previousMonthTasteProfile = previousMonthTasteProfile(
                userId,
                requestedMonth
        );

        long drinkingRecordCount = historyRepository.countByUserIdAndRecordDateBetween(
                userId,
                currentRange.startDate(),
                currentRange.endDate()
        );
        List<MonthlyReportResponse.FrequentCocktail> frequentCocktails = toFrequentCocktails(
                historyRepository.findFrequentCocktails(
                        userId,
                        currentRange.startDate(),
                        currentRange.endDate(),
                        PageRequest.of(0, TOP_COCKTAIL_LIMIT)
                ),
                drinkingRecordCount
        );

        return new MonthlyReportResponse(
                requestedMonth.getYear(),
                requestedMonth.getMonthValue(),
                toMonthlyMoodType(moodTypeCounts.get(0).moodType()),
                toRankedMoodTypes(moodTypeCounts),
                averageTasteProfile,
                toDisplayScores(averageTasteProfile),
                previousMonthTasteProfile,
                previousMonthTasteProfile == null ? null : toDisplayScores(previousMonthTasteProfile),
                frequentCocktails,
                new MonthlyReportResponse.Activity(currentResults.size(), drinkingRecordCount)
        );
    }

    private YearMonth parseRequestedMonth(int year, int month) {
        try {
            return HistoryDatePolicy.parseYearMonth(year, month);
        } catch (RestApiException exception) {
            throw new RestApiException(INVALID_REQUEST);
        }
    }

    private MonthlyReportResponse.TasteProfile previousMonthTasteProfile(Long userId, YearMonth requestedMonth) {
        if (HistoryDatePolicy.isMinimumSupportedMonth(requestedMonth)) {
            return null;
        }
        YearMonth previousMonth = requestedMonth.minusMonths(1);
        List<MoodTestResult> previousResults = moodTestResultRepository.findAllWithMoodType(
                userId,
                previousMonth.atDay(1),
                previousMonth.atEndOfMonth()
        );
        return previousResults.isEmpty() ? null : averageTasteProfile(previousResults);
    }

    private DateRange dateRange(YearMonth requestedMonth, LocalDate today) {
        LocalDate endDate = requestedMonth.equals(YearMonth.from(today))
                ? today
                : requestedMonth.atEndOfMonth();
        return new DateRange(requestedMonth.atDay(1), endDate);
    }

    private List<MoodTypeCount> countMoodTypes(List<MoodTestResult> results) {
        Map<Long, MoodTypeCount> countsByMoodTypeId = new LinkedHashMap<>();
        for (MoodTestResult result : results) {
            MoodType moodType = result.getMoodType();
            countsByMoodTypeId.compute(
                    moodType.getId(),
                    (id, count) -> count == null
                            ? new MoodTypeCount(moodType, 1)
                            : new MoodTypeCount(count.moodType(), count.count() + 1)
            );
        }
        Collator koreanCollator = Collator.getInstance(Locale.KOREAN);
        return countsByMoodTypeId.values().stream()
                .sorted(Comparator.comparingLong(MoodTypeCount::count).reversed()
                        .thenComparing(
                                count -> count.moodType().getName(),
                                koreanCollator
                        )
                        .thenComparingLong(count -> count.moodType().getId()))
                .toList();
    }

    private MonthlyReportResponse.TasteProfile averageTasteProfile(List<MoodTestResult> results) {
        BigDecimal count = BigDecimal.valueOf(results.size());
        return new MonthlyReportResponse.TasteProfile(
                average(results, MoodTestResult::getAlcoholIntensity, count),
                average(results, MoodTestResult::getSweetness, count),
                average(results, MoodTestResult::getSourness, count),
                average(results, MoodTestResult::getRefreshing, count),
                average(results, MoodTestResult::getBitterness, count)
        );
    }

    private BigDecimal average(
            List<MoodTestResult> results,
            java.util.function.Function<MoodTestResult, BigDecimal> extractor,
            BigDecimal count
    ) {
        BigDecimal sum = results.stream()
                .map(extractor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(count, 1, RoundingMode.HALF_UP);
    }

    private MonthlyReportResponse.DisplayTasteScores toDisplayScores(
            MonthlyReportResponse.TasteProfile profile
    ) {
        return new MonthlyReportResponse.DisplayTasteScores(
                toDisplayScore(profile.alcoholIntensity()),
                toDisplayScore(profile.sweetness()),
                toDisplayScore(profile.sourness()),
                toDisplayScore(profile.refreshing()),
                toDisplayScore(profile.bitterness())
        );
    }

    private int toDisplayScore(BigDecimal score) {
        int value = score.subtract(BigDecimal.ONE)
                .multiply(DISPLAY_SCORE_MULTIPLIER)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
        return Math.max(0, Math.min(100, value));
    }

    private MonthlyReportResponse.MoodType toMonthlyMoodType(MoodType moodType) {
        return new MonthlyReportResponse.MoodType(
                moodType.getId(),
                moodType.getCode(),
                moodType.getName(),
                moodType.getShortDescription(),
                moodType.getCharacterQuote(),
                imageUrl(moodType.getCharacterImage())
        );
    }

    private List<MonthlyReportResponse.RankedMoodType> toRankedMoodTypes(List<MoodTypeCount> counts) {
        List<MonthlyReportResponse.RankedMoodType> rankedMoodTypes = new ArrayList<>();
        long previousCount = -1;
        int ranking = 0;
        for (int index = 0; index < Math.min(counts.size(), TOP_MOOD_TYPE_LIMIT); index++) {
            MoodTypeCount count = counts.get(index);
            if (count.count() != previousCount) {
                ranking = index + 1;
                previousCount = count.count();
            }
            MoodType moodType = count.moodType();
            rankedMoodTypes.add(new MonthlyReportResponse.RankedMoodType(
                    moodType.getId(),
                    moodType.getCode(),
                    moodType.getName(),
                    imageUrl(moodType.getCharacterImage()),
                    count.count(),
                    ranking
            ));
        }
        return List.copyOf(rankedMoodTypes);
    }

    private List<MonthlyReportResponse.FrequentCocktail> toFrequentCocktails(
            List<HistoryRepository.FrequentCocktail> cocktails,
            long drinkingRecordCount
    ) {
        List<MonthlyReportResponse.FrequentCocktail> rankedCocktails = new ArrayList<>();
        long previousCount = -1;
        int ranking = 0;
        for (int index = 0; index < cocktails.size(); index++) {
            HistoryRepository.FrequentCocktail cocktail = cocktails.get(index);
            if (cocktail.getRecordCount() != previousCount) {
                ranking = index + 1;
                previousCount = cocktail.getRecordCount();
            }
            rankedCocktails.add(new MonthlyReportResponse.FrequentCocktail(
                    cocktail.getCocktailId(),
                    cocktail.getNameKo(),
                    cocktail.getNameEn(),
                    cocktail.getShortDescription(),
                    cocktail.getImageUrl(),
                    cocktail.getRecordCount(),
                    toRecordPercentage(cocktail.getRecordCount(), drinkingRecordCount),
                    ranking
            ));
        }
        return List.copyOf(rankedCocktails);
    }

    private int toRecordPercentage(long recordCount, long totalRecordCount) {
        if (totalRecordCount == 0) {
            return 0;
        }
        return BigDecimal.valueOf(recordCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalRecordCount), 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private String imageUrl(Image image) {
        return image == null ? null : image.getImageUrl();
    }

    private record DateRange(LocalDate startDate, LocalDate endDate) {
    }

    private record MoodTypeCount(MoodType moodType, long count) {
    }
}
