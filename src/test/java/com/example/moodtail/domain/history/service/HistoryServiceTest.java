package com.example.moodtail.domain.history.service;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.cocktail.repository.CocktailRepository;
import com.example.moodtail.domain.history.dto.request.HistoryCreateRequest;
import com.example.moodtail.domain.history.dto.request.HistoryUpdateRequest;
import com.example.moodtail.domain.history.entity.DrinkingRecord;
import com.example.moodtail.domain.history.repository.HistoryMoodTestResultRepository;
import com.example.moodtail.domain.history.repository.HistoryPhotoRepository;
import com.example.moodtail.domain.history.repository.HistoryRecommendationRepository;
import com.example.moodtail.domain.history.repository.HistoryRepository;
import com.example.moodtail.domain.moodtest.entity.MoodTestResult;
import com.example.moodtail.domain.moodtest.entity.MoodType;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

    private static final Long USER_ID = 1L;
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-07-11T03:30:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    @Mock
    private HistoryRepository historyRepository;
    @Mock
    private HistoryPhotoRepository historyPhotoRepository;
    @Mock
    private HistoryMoodTestResultRepository moodTestResultRepository;
    @Mock
    private HistoryRecommendationRepository recommendationRepository;
    @Mock
    private CocktailRepository cocktailRepository;
    @Mock
    private UserRepository userRepository;

    private HistoryService historyService;

    @BeforeEach
    void setUp() {
        historyService = new HistoryService(
                historyRepository,
                historyPhotoRepository,
                moodTestResultRepository,
                recommendationRepository,
                cocktailRepository,
                userRepository,
                CLOCK
        );
    }

    @Test
    void calendarCombinesTestResultsAndDrinkingRecords() {
        MoodType moodType = org.mockito.Mockito.mock(MoodType.class);
        when(moodType.getId()).thenReturn(3L);
        when(moodType.getCode()).thenReturn("FRESH_SPARK");
        when(moodType.getName()).thenReturn("상큼주의자");
        MoodTestResult result = org.mockito.Mockito.mock(MoodTestResult.class);
        when(result.getId()).thenReturn(10L);
        when(result.getResultDate()).thenReturn(LocalDate.of(2026, 7, 5));
        when(result.getMoodType()).thenReturn(moodType);
        when(moodTestResultRepository.findAllWithMoodType(
                USER_ID,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 11)
        )).thenReturn(List.of(result));
        when(historyRepository.findRecordDates(
                USER_ID,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 11)
        )).thenReturn(List.of(
                LocalDate.of(2026, 7, 5),
                LocalDate.of(2026, 7, 5)
        ));
        when(historyRepository.countByUserIdAndRecordDateBetween(
                USER_ID,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 11)
        )).thenReturn(2L);

        var response = historyService.getCalendar(USER_ID, 2026, 7);

        assertThat(response.testResultCount()).isEqualTo(1);
        assertThat(response.drinkingRecordCount()).isEqualTo(2);
        assertThat(response.reportRequiredTestCount()).isEqualTo(5);
        assertThat(response.reportAvailable()).isFalse();
        assertThat(response.days()).singleElement().satisfies(day -> {
            assertThat(day.hasTestResult()).isTrue();
            assertThat(day.hasDrinkingRecord()).isTrue();
            assertThat(day.photoCount()).isZero();
        });
    }

    @Test
    void returnsMonthlyTestResultsInLatestFirstRepositoryOrder() {
        MoodType moodType = org.mockito.Mockito.mock(MoodType.class);
        MoodTestResult latestResult = org.mockito.Mockito.mock(MoodTestResult.class);
        MoodTestResult olderResult = org.mockito.Mockito.mock(MoodTestResult.class);
        when(latestResult.getId()).thenReturn(23L);
        when(latestResult.getResultDate()).thenReturn(LocalDate.of(2026, 7, 10));
        when(latestResult.getMoodType()).thenReturn(moodType);
        when(olderResult.getId()).thenReturn(18L);
        when(olderResult.getResultDate()).thenReturn(LocalDate.of(2026, 7, 5));
        when(olderResult.getMoodType()).thenReturn(moodType);
        when(moodTestResultRepository.findAllWithMoodType(
                USER_ID,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 11)
        )).thenReturn(List.of(latestResult, olderResult));

        var response = historyService.getCalendar(USER_ID, 2026, 7);

        assertThat(response.testResults())
                .extracting(result -> result.resultDate())
                .containsExactly(
                        LocalDate.of(2026, 7, 10),
                        LocalDate.of(2026, 7, 5)
                );
        assertThat(response.testResults())
                .extracting(result -> result.resultId())
                .containsExactly(23L, 18L);
    }

    @Test
    void keepsLatestTestResultAsCalendarRepresentativeForSameDate() {
        LocalDate resultDate = LocalDate.of(2026, 7, 10);
        MoodType latestMoodType = org.mockito.Mockito.mock(MoodType.class);
        MoodType olderMoodType = org.mockito.Mockito.mock(MoodType.class);
        MoodTestResult latestResult = org.mockito.Mockito.mock(MoodTestResult.class);
        MoodTestResult olderResult = org.mockito.Mockito.mock(MoodTestResult.class);
        when(latestMoodType.getId()).thenReturn(3L);
        when(latestMoodType.getCode()).thenReturn("LATEST");
        when(latestMoodType.getName()).thenReturn("최신 무드");
        when(olderMoodType.getId()).thenReturn(2L);
        when(olderMoodType.getCode()).thenReturn("OLDER");
        when(olderMoodType.getName()).thenReturn("이전 무드");
        when(latestResult.getId()).thenReturn(23L);
        when(latestResult.getResultDate()).thenReturn(resultDate);
        when(latestResult.getMoodType()).thenReturn(latestMoodType);
        when(olderResult.getId()).thenReturn(18L);
        when(olderResult.getResultDate()).thenReturn(resultDate);
        when(olderResult.getMoodType()).thenReturn(olderMoodType);
        when(moodTestResultRepository.findAllWithMoodType(
                USER_ID,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 11)
        )).thenReturn(List.of(latestResult, olderResult));

        var response = historyService.getCalendar(USER_ID, 2026, 7);

        assertThat(response.days()).singleElement().satisfies(day -> {
            assertThat(day.date()).isEqualTo(resultDate);
            assertThat(day.moodType().moodTypeId()).isEqualTo(3L);
            assertThat(day.moodType().typeCode()).isEqualTo("LATEST");
        });
        assertThat(response.testResults())
                .extracting(result -> result.resultId())
                .containsExactly(23L, 18L);
    }

    @Test
    void returnsAllDrinkingRecordsForDateInRepositoryOrder() {
        LocalDate recordDate = LocalDate.of(2026, 7, 10);
        Cocktail mojito = cocktail(
                7L,
                "모히토",
                "민트와 라임의 청량한 만남",
                new BigDecimal("20.0")
        );
        Cocktail negroni = cocktail(8L, "네그로니", null, null);
        DrinkingRecord first = DrinkingRecord.create(
                User.createMember("회원", LocalDateTime.now(CLOCK)),
                mojito,
                recordDate,
                LocalDateTime.of(2026, 7, 10, 18, 0)
        );
        DrinkingRecord second = DrinkingRecord.create(
                User.createMember("회원", LocalDateTime.now(CLOCK)),
                negroni,
                recordDate,
                LocalDateTime.of(2026, 7, 10, 19, 0)
        );
        ReflectionTestUtils.setField(first, "id", 31L);
        ReflectionTestUtils.setField(second, "id", 32L);
        when(historyRepository.findAllWithDetailsByUserIdAndRecordDate(USER_ID, recordDate))
                .thenReturn(List.of(first, second));
        when(historyPhotoRepository.findAllByUserIdAndRecordDate(USER_ID, recordDate))
                .thenReturn(List.of());

        var response = historyService.getByDate(USER_ID, "2026-07-10");

        assertThat(response.drinkingRecords())
                .extracting(
                        item -> item.recordId(),
                        item -> item.cocktailName(),
                        item -> item.shortDescription(),
                        item -> item.alcoholDegree()
                )
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(
                                31L,
                                "모히토",
                                "민트와 라임의 청량한 만남",
                                new BigDecimal("20.0")
                        ),
                        org.assertj.core.groups.Tuple.tuple(32L, "네그로니", null, null)
                );
    }

    @Test
    void includesPhotoOnlyDateInCalendarWithPhotoCount() {
        LocalDate photoDate = LocalDate.of(2026, 7, 8);
        HistoryPhotoRepository.PhotoCountByDate photoCount =
                org.mockito.Mockito.mock(HistoryPhotoRepository.PhotoCountByDate.class);
        when(photoCount.getRecordDate()).thenReturn(photoDate);
        when(photoCount.getPhotoCount()).thenReturn(2L);
        when(historyPhotoRepository.findPhotoCountsByUserIdAndRecordDateBetween(
                USER_ID,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 11)
        )).thenReturn(List.of(photoCount));

        var response = historyService.getCalendar(USER_ID, 2026, 7);

        assertThat(response.days()).singleElement().satisfies(day -> {
            assertThat(day.date()).isEqualTo(photoDate);
            assertThat(day.hasTestResult()).isFalse();
            assertThat(day.hasDrinkingRecord()).isFalse();
            assertThat(day.photoCount()).isEqualTo(2L);
            assertThat(day.moodType()).isNull();
        });
        verify(historyPhotoRepository).findPhotoCountsByUserIdAndRecordDateBetween(
                USER_ID,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 11)
        );
    }

    @Test
    void rejectsFutureCalendarWithoutQueryingRepositories() {
        assertThatThrownBy(() -> historyService.getCalendar(USER_ID, 2026, 8))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("HISTORY_400"));

        verify(moodTestResultRepository, never()).findAllWithMoodType(any(), any(), any());
    }

    @Test
    void createsDifferentCocktailOnDateWithNoMatchingCocktailRecord() {
        User user = User.createMember("회원", LocalDateTime.now(CLOCK));
        Cocktail cocktail = org.mockito.Mockito.mock(Cocktail.class);
        LocalDate recordDate = LocalDate.of(2026, 7, 10);
        when(historyRepository.existsByUserIdAndRecordDateAndCocktailId(USER_ID, recordDate, 7L))
                .thenReturn(false);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(cocktailRepository.findById(7L)).thenReturn(Optional.of(cocktail));
        when(historyRepository.saveAndFlush(any(DrinkingRecord.class))).thenAnswer(invocation -> {
            DrinkingRecord record = invocation.getArgument(0);
            ReflectionTestUtils.setField(record, "id", 31L);
            return record;
        });
        var response = historyService.create(
                USER_ID,
                new HistoryCreateRequest(7L, recordDate)
        );

        assertThat(response.recordId()).isEqualTo(31L);
    }

    @Test
    void rejectsSameCocktailForTheSameUserAndDateBeforeLoadingEntities() {
        LocalDate recordDate = LocalDate.of(2026, 7, 10);
        when(historyRepository.existsByUserIdAndRecordDateAndCocktailId(USER_ID, recordDate, 7L))
                .thenReturn(true);

        assertThatThrownBy(() -> historyService.create(
                USER_ID,
                new HistoryCreateRequest(7L, recordDate)
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("HISTORY_409")
        );

        verify(userRepository, never()).findById(any());
        verify(cocktailRepository, never()).findById(any());
        verify(historyRepository, never()).saveAndFlush(any());
    }

    @Test
    void translatesOnlyTheUserDateCocktailUniqueConstraintRaceToDuplicateRecord() {
        User user = User.createMember("회원", LocalDateTime.now(CLOCK));
        Cocktail cocktail = org.mockito.Mockito.mock(Cocktail.class);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(cocktailRepository.findById(7L)).thenReturn(Optional.of(cocktail));
        when(historyRepository.saveAndFlush(any(DrinkingRecord.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "Duplicate entry for key 'uk_drinking_record_user_date_cocktail'"
                ));

        assertThatThrownBy(() -> historyService.create(
                USER_ID,
                new HistoryCreateRequest(7L, LocalDate.of(2026, 7, 10))
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("HISTORY_409")
        );
    }

    @Test
    void mapsAnUnrelatedDatabaseIntegrityFailureToTheInternalErrorContract() {
        User user = User.createMember("회원", LocalDateTime.now(CLOCK));
        Cocktail cocktail = org.mockito.Mockito.mock(Cocktail.class);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(cocktailRepository.findById(7L)).thenReturn(Optional.of(cocktail));
        DataIntegrityViolationException databaseFailure =
                new DataIntegrityViolationException("foreign key failure");
        when(historyRepository.saveAndFlush(any(DrinkingRecord.class))).thenThrow(databaseFailure);

        assertThatThrownBy(() -> historyService.create(
                USER_ID,
                new HistoryCreateRequest(7L, LocalDate.of(2026, 7, 10))
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("COMMON500")
        );
    }

    @Test
    void updatesOnlyProvidedCocktailAndDate() {
        Cocktail original = org.mockito.Mockito.mock(Cocktail.class);
        Cocktail replacement = org.mockito.Mockito.mock(Cocktail.class);
        when(replacement.getId()).thenReturn(8L);
        DrinkingRecord record = DrinkingRecord.create(
                User.createMember("회원", LocalDateTime.now(CLOCK)),
                original,
                LocalDate.of(2026, 7, 9),
                LocalDateTime.now(CLOCK)
        );
        ReflectionTestUtils.setField(record, "id", 31L);
        when(historyRepository.findOwnedForUpdate(31L, USER_ID)).thenReturn(Optional.of(record));
        when(cocktailRepository.findById(8L)).thenReturn(Optional.of(replacement));

        historyService.update(
                USER_ID,
                31L,
                new HistoryUpdateRequest(8L, LocalDate.of(2026, 7, 10))
        );

        assertThat(record.getCocktail()).isSameAs(replacement);
        assertThat(record.getRecordDate()).isEqualTo(LocalDate.of(2026, 7, 10));
        verify(historyRepository).existsByUserIdAndRecordDateAndCocktailIdAndIdNot(
                USER_ID,
                LocalDate.of(2026, 7, 10),
                8L,
                31L
        );
    }

    @Test
    void rejectsEmptyUpdate() {
        assertThatThrownBy(() -> historyService.update(
                USER_ID,
                31L,
                new HistoryUpdateRequest(null, null)
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("HISTORY_400"));

        verify(historyRepository, never()).findOwnedForUpdate(any(), any());
    }

    @Test
    void rejectsMovingARecordToDateWithTheSameCocktail() {
        Cocktail cocktail = org.mockito.Mockito.mock(Cocktail.class);
        when(cocktail.getId()).thenReturn(7L);
        DrinkingRecord record = DrinkingRecord.create(
                User.createMember("회원", LocalDateTime.now(CLOCK)),
                cocktail,
                LocalDate.of(2026, 7, 9),
                LocalDateTime.now(CLOCK)
        );
        ReflectionTestUtils.setField(record, "id", 31L);
        LocalDate occupiedDate = LocalDate.of(2026, 7, 10);
        when(historyRepository.findOwnedForUpdate(31L, USER_ID)).thenReturn(Optional.of(record));
        when(historyRepository.existsByUserIdAndRecordDateAndCocktailIdAndIdNot(
                USER_ID,
                occupiedDate,
                7L,
                31L
        ))
                .thenReturn(true);

        assertThatThrownBy(() -> historyService.update(
                USER_ID,
                31L,
                new HistoryUpdateRequest(null, occupiedDate)
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("HISTORY_409")
        );

        assertThat(record.getRecordDate()).isEqualTo(LocalDate.of(2026, 7, 9));
    }

    @Test
    void rejectsChangingToCocktailAlreadyRecordedOnTheSameDate() {
        Cocktail original = org.mockito.Mockito.mock(Cocktail.class);
        Cocktail duplicate = org.mockito.Mockito.mock(Cocktail.class);
        when(duplicate.getId()).thenReturn(8L);
        LocalDate recordDate = LocalDate.of(2026, 7, 10);
        DrinkingRecord record = DrinkingRecord.create(
                User.createMember("회원", LocalDateTime.now(CLOCK)),
                original,
                recordDate,
                LocalDateTime.now(CLOCK)
        );
        ReflectionTestUtils.setField(record, "id", 31L);
        when(historyRepository.findOwnedForUpdate(31L, USER_ID)).thenReturn(Optional.of(record));
        when(cocktailRepository.findById(8L)).thenReturn(Optional.of(duplicate));
        when(historyRepository.existsByUserIdAndRecordDateAndCocktailIdAndIdNot(
                USER_ID,
                recordDate,
                8L,
                31L
        )).thenReturn(true);

        assertThatThrownBy(() -> historyService.update(
                USER_ID,
                31L,
                new HistoryUpdateRequest(8L, null)
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("HISTORY_409")
        );

        assertThat(record.getCocktail()).isSameAs(original);
        assertThat(record.getRecordDate()).isEqualTo(recordDate);
    }

    @Test
    void deletesOnlyDrinkingRecordBecausePhotosBelongToDate() {
        DrinkingRecord record = DrinkingRecord.create(
                User.createMember("회원", LocalDateTime.now(CLOCK)),
                org.mockito.Mockito.mock(Cocktail.class),
                LocalDate.of(2026, 7, 10),
                LocalDateTime.now(CLOCK)
        );
        ReflectionTestUtils.setField(record, "id", 31L);
        when(historyRepository.findOwnedForUpdate(31L, USER_ID)).thenReturn(Optional.of(record));

        historyService.delete(USER_ID, 31L);

        verify(historyRepository).delete(record);
        verify(historyRepository).flush();
    }

    @Test
    void hidesAnotherUsersTestResultAsNotFound() {
        when(moodTestResultRepository.findDetailByIdAndUserId(10L, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> historyService.getTestResultDetail(USER_ID, 10L))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("HISTORY_TEST_404"));
    }

    @Test
    void rejectsNonPositiveResourceIdBeforeQueryingRepository() {
        assertThatThrownBy(() -> historyService.getDetail(USER_ID, 0L))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("HISTORY_400"));

        verify(historyRepository, never()).findWithDetailsByIdAndUserId(any(), any());
    }

    private Cocktail cocktail(
            Long id,
            String name,
            String shortDescription,
            BigDecimal alcoholDegree
    ) {
        Cocktail cocktail = org.mockito.Mockito.mock(Cocktail.class);
        when(cocktail.getId()).thenReturn(id);
        when(cocktail.getNameKo()).thenReturn(name);
        when(cocktail.getShortDescription()).thenReturn(shortDescription);
        when(cocktail.getAlcoholDegree()).thenReturn(alcoholDegree);
        return cocktail;
    }
}
