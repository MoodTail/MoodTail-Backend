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
        )).thenReturn(List.of(LocalDate.of(2026, 7, 5)));

        var response = historyService.getCalendar(USER_ID, 2026, 7);

        assertThat(response.testResultCount()).isEqualTo(1);
        assertThat(response.drinkingRecordCount()).isEqualTo(1);
        assertThat(response.reportRequiredTestCount()).isEqualTo(5);
        assertThat(response.reportAvailable()).isFalse();
        assertThat(response.days()).singleElement().satisfies(day -> {
            assertThat(day.hasTestResult()).isTrue();
            assertThat(day.hasDrinkingRecord()).isTrue();
        });
    }

    @Test
    void rejectsFutureCalendarWithoutQueryingRepositories() {
        assertThatThrownBy(() -> historyService.getCalendar(USER_ID, 2026, 8))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("HISTORY_400"));

        verify(moodTestResultRepository, never()).findAllWithMoodType(any(), any(), any());
    }

    @Test
    void createsDrinkingRecord() {
        User user = User.createMember("회원", LocalDateTime.now(CLOCK));
        Cocktail cocktail = org.mockito.Mockito.mock(Cocktail.class);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(cocktailRepository.findById(7L)).thenReturn(Optional.of(cocktail));
        when(historyRepository.saveAndFlush(any(DrinkingRecord.class))).thenAnswer(invocation -> {
            DrinkingRecord record = invocation.getArgument(0);
            ReflectionTestUtils.setField(record, "id", 31L);
            return record;
        });
        var response = historyService.create(
                USER_ID,
                new HistoryCreateRequest(7L, LocalDate.of(2026, 7, 10))
        );

        assertThat(response.recordId()).isEqualTo(31L);
    }

    @Test
    void rejectsSecondDrinkingRecordForTheSameUserAndDateBeforeLoadingEntities() {
        LocalDate recordDate = LocalDate.of(2026, 7, 10);
        when(historyRepository.existsByUserIdAndRecordDate(USER_ID, recordDate)).thenReturn(true);

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
    void translatesOnlyTheUserDateUniqueConstraintRaceToDuplicateRecord() {
        User user = User.createMember("회원", LocalDateTime.now(CLOCK));
        Cocktail cocktail = org.mockito.Mockito.mock(Cocktail.class);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(cocktailRepository.findById(7L)).thenReturn(Optional.of(cocktail));
        when(historyRepository.saveAndFlush(any(DrinkingRecord.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "Duplicate entry for key 'uk_drinking_record_user_date'"
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
    void rejectsMovingARecordToADateThatAlreadyHasARecord() {
        DrinkingRecord record = DrinkingRecord.create(
                User.createMember("회원", LocalDateTime.now(CLOCK)),
                org.mockito.Mockito.mock(Cocktail.class),
                LocalDate.of(2026, 7, 9),
                LocalDateTime.now(CLOCK)
        );
        ReflectionTestUtils.setField(record, "id", 31L);
        LocalDate occupiedDate = LocalDate.of(2026, 7, 10);
        when(historyRepository.findOwnedForUpdate(31L, USER_ID)).thenReturn(Optional.of(record));
        when(historyRepository.existsByUserIdAndRecordDateAndIdNot(USER_ID, occupiedDate, 31L))
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
}
