package com.example.moodtail.domain.report.service;

import com.example.moodtail.domain.history.repository.HistoryRepository;
import com.example.moodtail.domain.history.repository.HistoryMoodTestResultRepository;
import com.example.moodtail.domain.moodtest.entity.MoodTestResult;
import com.example.moodtail.domain.moodtest.entity.MoodType;
import com.example.moodtail.global.common.exception.RestApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthlyReportServiceTest {

    private static final Long USER_ID = 1L;
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-07-11T03:30:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    @Mock
    private HistoryMoodTestResultRepository moodTestResultRepository;
    @Mock
    private HistoryRepository historyRepository;

    private MonthlyReportService monthlyReportService;

    @BeforeEach
    void setUp() {
        monthlyReportService = new MonthlyReportService(
                moodTestResultRepository,
                historyRepository,
                CLOCK
        );
    }

    @Test
    void aggregatesMonthlyReportAndPreviousMonthComparison() {
        MoodType firstMoodType = moodType(1L, "FRESH_SPARK", "상큼주의자");
        MoodType secondMoodType = moodType(2L, "CALM_DEPTH", "차분주의자");
        List<MoodTestResult> currentResults = List.of(
                result(firstMoodType, "3.0", "2.0", "4.0", "5.0", "1.0"),
                result(secondMoodType, "4.0", "3.0", "3.0", "4.0", "2.0"),
                result(firstMoodType, "3.0", "2.0", "4.0", "5.0", "1.0"),
                result(secondMoodType, "4.0", "3.0", "3.0", "4.0", "2.0"),
                result(firstMoodType, "3.0", "2.0", "4.0", "5.0", "1.0")
        );
        when(moodTestResultRepository.findAllWithMoodType(
                USER_ID,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 11)
        )).thenReturn(currentResults);
        MoodTestResult previousResult = tasteResult("2.0", "2.0", "2.0", "2.0", "2.0");
        when(moodTestResultRepository.findAllWithMoodType(
                USER_ID,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30)
        )).thenReturn(List.of(previousResult));
        when(historyRepository.countByUserIdAndRecordDateBetween(
                USER_ID,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 11)
        )).thenReturn(4L);
        HistoryRepository.FrequentCocktail frequentCocktail = frequentCocktail(
                7L,
                "모히토",
                "Mojito",
                3L
        );
        when(historyRepository.findFrequentCocktails(
                eq(USER_ID),
                eq(LocalDate.of(2026, 7, 1)),
                eq(LocalDate.of(2026, 7, 11)),
                any(Pageable.class)
        )).thenReturn(List.of(frequentCocktail));

        var response = monthlyReportService.getMonthlyReport(USER_ID, 2026, 7);

        assertThat(response.monthlyMoodType().moodTypeId()).isEqualTo(1L);
        assertThat(response.topMoodTypes()).extracting(type -> type.moodTypeId(), type -> type.count())
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(1L, 3L),
                        org.assertj.core.groups.Tuple.tuple(2L, 2L)
                );
        assertThat(response.averageTasteProfile().alcoholIntensity()).isEqualByComparingTo("3.4");
        assertThat(response.displayAverageTasteScores().alcoholIntensity()).isEqualTo(60);
        assertThat(response.previousMonthTasteProfile().alcoholIntensity()).isEqualByComparingTo("2.0");
        assertThat(response.previousMonthDisplayTasteScores().alcoholIntensity()).isEqualTo(25);
        assertThat(response.activity().testCount()).isEqualTo(5);
        assertThat(response.activity().drinkingRecordCount()).isEqualTo(4);
    }

    @Test
    void usesKoreanNameOrderAndSharedRankForMoodTypeTies() {
        MoodType secondAlphabetically = moodType(1L, "NA", "나 타입");
        MoodType firstAlphabetically = moodType(2L, "GA", "가 타입");
        MoodType third = moodType(3L, "DA", "다 타입");
        stubCurrentMonth(List.of(
                result(secondAlphabetically),
                result(firstAlphabetically),
                result(secondAlphabetically),
                result(firstAlphabetically),
                result(third)
        ), List.of());

        var response = monthlyReportService.getMonthlyReport(USER_ID, 2026, 7);

        assertThat(response.topMoodTypes()).extracting(
                type -> type.moodTypeId(),
                type -> type.ranking()
        ).containsExactly(
                org.assertj.core.groups.Tuple.tuple(2L, 1),
                org.assertj.core.groups.Tuple.tuple(1L, 1),
                org.assertj.core.groups.Tuple.tuple(3L, 3)
        );
    }

    @Test
    void assignsSharedRankToCocktailTies() {
        MoodType moodType = moodType(1L, "FRESH", "상큼");
        stubCurrentMonth(
                List.of(
                        result(moodType),
                        result(moodType),
                        result(moodType),
                        result(moodType),
                        result(moodType)
                ),
                List.of(
                        frequentCocktail(1L, "가", "A", 3),
                        frequentCocktail(2L, "나", "B", 3),
                        frequentCocktail(3L, "다", "C", 1)
                )
        );

        var response = monthlyReportService.getMonthlyReport(USER_ID, 2026, 7);

        assertThat(response.frequentCocktails()).extracting(
                item -> item.cocktailId(),
                item -> item.ranking()
        ).containsExactly(
                org.assertj.core.groups.Tuple.tuple(1L, 1),
                org.assertj.core.groups.Tuple.tuple(2L, 1),
                org.assertj.core.groups.Tuple.tuple(3L, 3)
        );
    }

    @Test
    void rejectsReportWhenCurrentMonthHasFewerThanFiveResults() {
        when(moodTestResultRepository.findAllWithMoodType(
                USER_ID,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 11)
        )).thenReturn(List.of(org.mockito.Mockito.mock(MoodTestResult.class)));

        assertThatThrownBy(() -> monthlyReportService.getMonthlyReport(USER_ID, 2026, 7))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("REPORT_409"));

        verify(historyRepository, never()).findFrequentCocktails(any(), any(), any(), any());
    }

    @Test
    void rejectsFutureMonthBeforeQueryingRepositories() {
        assertThatThrownBy(() -> monthlyReportService.getMonthlyReport(USER_ID, 2026, 8))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("REPORT_400"));

        verify(moodTestResultRepository, never()).findAllWithMoodType(any(), any(), any());
    }

    private void stubCurrentMonth(
            List<MoodTestResult> currentResults,
            List<HistoryRepository.FrequentCocktail> cocktails
    ) {
        when(moodTestResultRepository.findAllWithMoodType(
                USER_ID,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 11)
        )).thenReturn(currentResults);
        when(moodTestResultRepository.findAllWithMoodType(
                USER_ID,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30)
        )).thenReturn(List.of());
        when(historyRepository.findFrequentCocktails(
                eq(USER_ID),
                eq(LocalDate.of(2026, 7, 1)),
                eq(LocalDate.of(2026, 7, 11)),
                any(Pageable.class)
        )).thenReturn(cocktails);
    }

    private MoodTestResult result(MoodType moodType) {
        return result(moodType, "3.0", "3.0", "3.0", "3.0", "3.0");
    }

    private MoodTestResult result(
            MoodType moodType,
            String alcoholIntensity,
            String sweetness,
            String sourness,
            String refreshing,
            String bitterness
    ) {
        MoodTestResult result = tasteResult(
                alcoholIntensity,
                sweetness,
                sourness,
                refreshing,
                bitterness
        );
        when(result.getMoodType()).thenReturn(moodType);
        return result;
    }

    private MoodTestResult tasteResult(
            String alcoholIntensity,
            String sweetness,
            String sourness,
            String refreshing,
            String bitterness
    ) {
        MoodTestResult result = org.mockito.Mockito.mock(MoodTestResult.class);
        when(result.getAlcoholIntensity()).thenReturn(new BigDecimal(alcoholIntensity));
        when(result.getSweetness()).thenReturn(new BigDecimal(sweetness));
        when(result.getSourness()).thenReturn(new BigDecimal(sourness));
        when(result.getRefreshing()).thenReturn(new BigDecimal(refreshing));
        when(result.getBitterness()).thenReturn(new BigDecimal(bitterness));
        return result;
    }

    private MoodType moodType(Long id, String code, String name) {
        MoodType moodType = org.mockito.Mockito.mock(MoodType.class);
        when(moodType.getId()).thenReturn(id);
        when(moodType.getCode()).thenReturn(code);
        when(moodType.getName()).thenReturn(name);
        return moodType;
    }

    private HistoryRepository.FrequentCocktail frequentCocktail(
            Long id,
            String nameKo,
            String nameEn,
            long count
    ) {
        HistoryRepository.FrequentCocktail cocktail = org.mockito.Mockito.mock(
                HistoryRepository.FrequentCocktail.class
        );
        when(cocktail.getCocktailId()).thenReturn(id);
        when(cocktail.getNameKo()).thenReturn(nameKo);
        when(cocktail.getNameEn()).thenReturn(nameEn);
        when(cocktail.getShortDescription()).thenReturn("설명");
        when(cocktail.getImageUrl()).thenReturn("https://cdn.example/cocktails/" + id + ".png");
        when(cocktail.getRecordCount()).thenReturn(count);
        return cocktail;
    }
}
