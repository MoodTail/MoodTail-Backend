package com.example.moodtail.domain.history.service;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.cocktail.repository.CocktailRepository;
import com.example.moodtail.domain.history.dto.request.HistoryCreateRequest;
import com.example.moodtail.domain.history.dto.request.HistoryUpdateRequest;
import com.example.moodtail.domain.history.entity.DrinkingRecord;
import com.example.moodtail.domain.history.entity.HistoryPhoto;
import com.example.moodtail.domain.history.repository.HistoryMoodTestResultRepository;
import com.example.moodtail.domain.history.repository.HistoryPhotoRepository;
import com.example.moodtail.domain.history.repository.HistoryRecommendationRepository;
import com.example.moodtail.domain.history.repository.HistoryRepository;
import com.example.moodtail.domain.image.entity.Image;
import com.example.moodtail.domain.image.entity.ImageSourceType;
import com.example.moodtail.domain.moodtest.entity.CompatibilityType;
import com.example.moodtail.domain.moodtest.entity.MoodTestResult;
import com.example.moodtail.domain.moodtest.entity.MoodType;
import com.example.moodtail.domain.moodtest.entity.MoodTypeCompatibility;
import com.example.moodtail.domain.moodtest.repository.MoodTypeCompatibilityRepository;
import com.example.moodtail.domain.recommendation.entity.RecommendationItem;
import com.example.moodtail.domain.recommendation.entity.RecommendationSessionType;
import com.example.moodtail.domain.recommendation.model.TasteProfile;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import com.example.moodtail.global.infra.s3.S3StorageException;
import com.example.moodtail.global.infra.s3.S3StorageService;
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
    private MoodTypeCompatibilityRepository moodTypeCompatibilityRepository;
    @Mock
    private CocktailRepository cocktailRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private S3StorageService storageService;

    private HistoryService historyService;

    @BeforeEach
    void setUp() {
        historyService = new HistoryService(
                historyRepository,
                historyPhotoRepository,
                moodTestResultRepository,
                recommendationRepository,
                moodTypeCompatibilityRepository,
                cocktailRepository,
                userRepository,
                storageService,
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
        HistoryRepository.MonthlyDrinkingRecordSummary firstRecord = monthlyRecord(
                LocalDate.of(2026, 7, 5),
                31L,
                7L,
                "모히토"
        );
        HistoryRepository.MonthlyDrinkingRecordSummary secondRecord = monthlyRecord(
                LocalDate.of(2026, 7, 5),
                32L,
                8L,
                "네그로니"
        );
        when(historyRepository.findMonthlyDrinkingRecordSummaries(
                USER_ID,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 11)
        )).thenReturn(List.of(firstRecord, secondRecord));

        var response = historyService.getCalendar(USER_ID, 2026, 7);

        assertThat(response.testResultCount()).isEqualTo(1);
        assertThat(response.drinkingRecordCount()).isEqualTo(2);
        assertThat(response.reportRequiredTestCount()).isEqualTo(5);
        assertThat(response.reportAvailable()).isFalse();
        assertThat(response.testResults()).singleElement().satisfies(monthlyResult ->
                assertThat(monthlyResult.drinkingRecords())
                        .extracting(
                                record -> record.recordId(),
                                record -> record.cocktailId(),
                                record -> record.cocktailName()
                        )
                        .containsExactly(
                                org.assertj.core.groups.Tuple.tuple(31L, 7L, "모히토"),
                                org.assertj.core.groups.Tuple.tuple(32L, 8L, "네그로니")
                        )
        );
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
    void returnsTemporaryAccessUrlsForAllPhotosOnTheDate() {
        LocalDate recordDate = LocalDate.of(2026, 7, 10);
        User user = User.createMember("회원", LocalDateTime.now(CLOCK));
        List<String> storedUrls = List.of(
                "https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/history/photos/"
                        + "8d5f57e1-40e5-46b2-852d-1c3dd640efb8.png",
                "https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/history/photos/"
                        + "49f0cc65-8c36-49b7-936d-10f06101cfba.webp",
                "https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/history/photos/"
                        + "73ac1330-c8ff-40f6-ab3d-e0076018b47d.jpg",
                "https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/history/photos/"
                        + "eab7a479-8471-4842-aeff-32331adba39b.jpeg",
                "https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/history/photos/"
                        + "6c498675-f058-45bc-a2cc-1e0fb782e8ac.png"
        );
        List<String> accessUrls = storedUrls.stream()
                .map(url -> url + "?X-Amz-Signature=signed")
                .toList();
        List<Image> images = storedUrls.stream()
                .map(url -> Image.create(url, ImageSourceType.GALLERY))
                .toList();
        List<HistoryPhoto> photos = images.stream()
                .map(image -> HistoryPhoto.create(user, recordDate, image))
                .toList();
        for (int index = 0; index < photos.size(); index++) {
            ReflectionTestUtils.setField(photos.get(index), "id", (long) index + 1);
            when(storageService.createPresignedGetUrl(storedUrls.get(index)))
                    .thenReturn(accessUrls.get(index));
        }
        when(historyPhotoRepository.findAllByUserIdAndRecordDate(USER_ID, recordDate))
                .thenReturn(photos);

        var response = historyService.getByDate(USER_ID, "2026-07-10");

        assertThat(response.photos())
                .extracting(photo -> photo.photoId(), photo -> photo.imageUrl())
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(1L, accessUrls.get(0)),
                        org.assertj.core.groups.Tuple.tuple(2L, accessUrls.get(1)),
                        org.assertj.core.groups.Tuple.tuple(3L, accessUrls.get(2)),
                        org.assertj.core.groups.Tuple.tuple(4L, accessUrls.get(3)),
                        org.assertj.core.groups.Tuple.tuple(5L, accessUrls.get(4))
                );
        assertThat(images)
                .extracting(Image::getImageUrl)
                .containsExactlyElementsOf(storedUrls);
    }

    @Test
    void mapsTemporaryPhotoAccessUrlFailureToHistoryContract() {
        LocalDate recordDate = LocalDate.of(2026, 7, 10);
        String storedUrl = "https://moodtail.s3.ap-northeast-2.amazonaws.com/history/photos/"
                + "8d5f57e1-40e5-46b2-852d-1c3dd640efb8.png";
        Image image = Image.create(storedUrl, ImageSourceType.GALLERY);
        HistoryPhoto photo = HistoryPhoto.create(
                User.createMember("회원", LocalDateTime.now(CLOCK)),
                recordDate,
                image
        );
        ReflectionTestUtils.setField(photo, "id", 3L);
        when(historyPhotoRepository.findAllByUserIdAndRecordDate(USER_ID, recordDate))
                .thenReturn(List.of(photo));
        when(storageService.createPresignedGetUrl(storedUrl))
                .thenThrow(new S3StorageException("presign failure"));

        assertThatThrownBy(() -> historyService.getByDate(USER_ID, "2026-07-10"))
                .isInstanceOfSatisfying(
                        RestApiException.class,
                        exception -> assertThat(exception.getErrorCode().getCode())
                                .isEqualTo("HISTORY_PHOTO503")
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
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("HISTORY400"));

        verify(moodTestResultRepository, never()).findAllWithMoodType(any(), any(), any());
    }

    @Test
    void createsMultipleCocktailsInRequestOrder() {
        User user = User.createMember("회원", LocalDateTime.now(CLOCK));
        Cocktail firstCocktail = org.mockito.Mockito.mock(Cocktail.class);
        Cocktail secondCocktail = org.mockito.Mockito.mock(Cocktail.class);
        LocalDate recordDate = LocalDate.of(2026, 7, 10);
        when(firstCocktail.getId()).thenReturn(7L);
        when(secondCocktail.getId()).thenReturn(8L);
        when(historyRepository.findExistingCocktailIds(
                USER_ID,
                recordDate,
                List.of(7L, 8L)
        )).thenReturn(List.of());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(cocktailRepository.findAllById(List.of(7L, 8L)))
                .thenReturn(List.of(secondCocktail, firstCocktail));
        when(historyRepository.saveAllAndFlush(any())).thenAnswer(invocation -> {
            List<DrinkingRecord> records = invocation.getArgument(0);
            ReflectionTestUtils.setField(records.get(0), "id", 31L);
            ReflectionTestUtils.setField(records.get(1), "id", 32L);
            return records;
        });
        var response = historyService.create(
                USER_ID,
                new HistoryCreateRequest(List.of(7L, 8L), recordDate)
        );

        assertThat(response).extracting(
                com.example.moodtail.domain.history.dto.response.HistoryCreateResponse::recordId
        ).containsExactly(31L, 32L);
        assertThat(response).extracting(
                com.example.moodtail.domain.history.dto.response.HistoryCreateResponse::cocktailId
        ).containsExactly(7L, 8L);
    }

    @Test
    void rejectsDuplicateCocktailIdsWithinRequestBeforeQueryingRepositories() {
        assertThatThrownBy(() -> historyService.create(
                USER_ID,
                new HistoryCreateRequest(
                        List.of(7L, 7L),
                        LocalDate.of(2026, 7, 10)
                )
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("HISTORY409")
        );

        verify(historyRepository, never()).findExistingCocktailIds(any(), any(), any());
        verify(userRepository, never()).findById(any());
        verify(cocktailRepository, never()).findAllById(any());
        verify(historyRepository, never()).saveAllAndFlush(any());
    }

    @Test
    void rejectsCocktailAlreadyRecordedOnTheSameDateBeforeLoadingEntities() {
        LocalDate recordDate = LocalDate.of(2026, 7, 10);
        when(historyRepository.findExistingCocktailIds(
                USER_ID,
                recordDate,
                List.of(7L, 8L)
        )).thenReturn(List.of(7L));

        assertThatThrownBy(() -> historyService.create(
                USER_ID,
                new HistoryCreateRequest(List.of(7L, 8L), recordDate)
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("HISTORY409")
        );

        verify(userRepository, never()).findById(any());
        verify(cocktailRepository, never()).findAllById(any());
        verify(historyRepository, never()).saveAllAndFlush(any());
    }

    @Test
    void rejectsEntireRequestWhenAnyCocktailDoesNotExist() {
        User user = User.createMember("회원", LocalDateTime.now(CLOCK));
        Cocktail existingCocktail = org.mockito.Mockito.mock(Cocktail.class);
        LocalDate recordDate = LocalDate.of(2026, 7, 10);
        when(existingCocktail.getId()).thenReturn(7L);
        when(historyRepository.findExistingCocktailIds(
                USER_ID,
                recordDate,
                List.of(7L, 8L)
        )).thenReturn(List.of());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(cocktailRepository.findAllById(List.of(7L, 8L)))
                .thenReturn(List.of(existingCocktail));

        assertThatThrownBy(() -> historyService.create(
                USER_ID,
                new HistoryCreateRequest(List.of(7L, 8L), recordDate)
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("COCKTAIL404")
        );

        verify(historyRepository, never()).saveAllAndFlush(any());
    }

    @Test
    void translatesOnlyTheUserDateCocktailUniqueConstraintRaceToDuplicateRecord() {
        User user = User.createMember("회원", LocalDateTime.now(CLOCK));
        Cocktail cocktail = org.mockito.Mockito.mock(Cocktail.class);
        LocalDate recordDate = LocalDate.of(2026, 7, 10);
        when(cocktail.getId()).thenReturn(7L);
        when(historyRepository.findExistingCocktailIds(
                USER_ID,
                recordDate,
                List.of(7L)
        )).thenReturn(List.of());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(cocktailRepository.findAllById(List.of(7L))).thenReturn(List.of(cocktail));
        when(historyRepository.saveAllAndFlush(any()))
                .thenThrow(new DataIntegrityViolationException(
                        "Duplicate entry for key 'uk_drinking_record_user_date_cocktail'"
                ));

        assertThatThrownBy(() -> historyService.create(
                USER_ID,
                new HistoryCreateRequest(List.of(7L), recordDate)
        )).isInstanceOfSatisfying(RestApiException.class, exception ->
                assertThat(exception.getErrorCode().getCode()).isEqualTo("HISTORY409")
        );
    }

    @Test
    void mapsAnUnrelatedDatabaseIntegrityFailureToTheInternalErrorContract() {
        User user = User.createMember("회원", LocalDateTime.now(CLOCK));
        Cocktail cocktail = org.mockito.Mockito.mock(Cocktail.class);
        LocalDate recordDate = LocalDate.of(2026, 7, 10);
        when(cocktail.getId()).thenReturn(7L);
        when(historyRepository.findExistingCocktailIds(
                USER_ID,
                recordDate,
                List.of(7L)
        )).thenReturn(List.of());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(cocktailRepository.findAllById(List.of(7L))).thenReturn(List.of(cocktail));
        DataIntegrityViolationException databaseFailure =
                new DataIntegrityViolationException("foreign key failure");
        when(historyRepository.saveAllAndFlush(any())).thenThrow(databaseFailure);

        assertThatThrownBy(() -> historyService.create(
                USER_ID,
                new HistoryCreateRequest(List.of(7L), recordDate)
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
                assertThat(exception.getErrorCode().getCode()).isEqualTo("HISTORY400"));

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
                assertThat(exception.getErrorCode().getCode()).isEqualTo("HISTORY409")
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
                assertThat(exception.getErrorCode().getCode()).isEqualTo("HISTORY409")
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
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("HISTORY_TEST404"));
    }

    @Test
    void returnsDisplayScoresCompatibilityAndRecommendationDescriptionForSavedResult() {
        MoodTestResult result = org.mockito.Mockito.mock(MoodTestResult.class);
        MoodType moodType = org.mockito.Mockito.mock(MoodType.class);
        MoodType bestMoodType = org.mockito.Mockito.mock(MoodType.class);
        MoodType worstMoodType = org.mockito.Mockito.mock(MoodType.class);
        MoodTypeCompatibility bestCompatibility = org.mockito.Mockito.mock(
                MoodTypeCompatibility.class
        );
        MoodTypeCompatibility worstCompatibility = org.mockito.Mockito.mock(
                MoodTypeCompatibility.class
        );
        RecommendationItem recommendation = org.mockito.Mockito.mock(RecommendationItem.class);
        Cocktail cocktail = org.mockito.Mockito.mock(Cocktail.class);

        when(result.getId()).thenReturn(10L);
        when(result.getResultDate()).thenReturn(LocalDate.of(2026, 7, 5));
        when(result.getMoodType()).thenReturn(moodType);
        when(result.toTasteProfile()).thenReturn(tasteProfile("1.0", "2.8", "3.0", "4.0", "5.0"));
        when(result.getAlcoholIntensity()).thenReturn(new BigDecimal("1.0"));
        when(result.getSweetness()).thenReturn(new BigDecimal("2.8"));
        when(result.getSourness()).thenReturn(new BigDecimal("3.0"));
        when(result.getRefreshing()).thenReturn(new BigDecimal("4.0"));
        when(result.getBitterness()).thenReturn(new BigDecimal("5.0"));

        when(moodType.getId()).thenReturn(3L);
        when(moodType.getCode()).thenReturn("FRESH_SPARK");
        when(moodType.getName()).thenReturn("상큼주의자");
        when(moodType.getShortDescription()).thenReturn("산뜻한 한 잔이 어울리는 타입");
        when(moodType.getDescription()).thenReturn("오늘은 산뜻한 기분을 즐겨보세요.");
        when(moodType.getCharacterQuote()).thenReturn("기분 좋은 상큼함을 시작해요.");
        when(moodType.toTasteProfile()).thenReturn(tasteProfile("5.0", "4.0", "3.0", "2.0", "1.0"));

        when(bestCompatibility.getCompatibilityType()).thenReturn(CompatibilityType.BEST);
        when(bestCompatibility.getTargetMoodType()).thenReturn(bestMoodType);
        when(bestMoodType.getId()).thenReturn(4L);
        when(bestMoodType.getCode()).thenReturn("BEST_TYPE");
        when(bestMoodType.getName()).thenReturn("이상주의자");
        when(worstCompatibility.getCompatibilityType()).thenReturn(CompatibilityType.WORST);
        when(worstCompatibility.getTargetMoodType()).thenReturn(worstMoodType);
        when(worstMoodType.getId()).thenReturn(5L);
        when(worstMoodType.getCode()).thenReturn("WORST_TYPE");
        when(worstMoodType.getName()).thenReturn("현실주의자");

        when(recommendation.getCocktail()).thenReturn(cocktail);
        when(recommendation.getRanking()).thenReturn(1);
        when(recommendation.getMatchScore()).thenReturn(95);
        when(cocktail.getId()).thenReturn(7L);
        when(cocktail.getNameKo()).thenReturn("모히토");
        when(cocktail.getShortDescription()).thenReturn("상쾌한 민트와 라임의 조화");

        when(moodTestResultRepository.findDetailByIdAndUserId(10L, USER_ID))
                .thenReturn(Optional.of(result));
        when(recommendationRepository.findByTestResult(
                USER_ID,
                10L,
                RecommendationSessionType.TEST_RESULT
        )).thenReturn(List.of(recommendation));
        when(moodTypeCompatibilityRepository.findAllByMoodTypeId(3L))
                .thenReturn(List.of(bestCompatibility, worstCompatibility));

        var response = historyService.getTestResultDetail(USER_ID, 10L);

        assertThat(response.displayTasteScores())
                .extracting(
                        scores -> scores.alcoholIntensity(),
                        scores -> scores.sweetness(),
                        scores -> scores.sourness(),
                        scores -> scores.refreshing(),
                        scores -> scores.bitterness()
                )
                .containsExactly(0, 45, 50, 75, 100);
        assertThat(response.moodType().displayTasteScores())
                .extracting(
                        scores -> scores.alcoholIntensity(),
                        scores -> scores.sweetness(),
                        scores -> scores.sourness(),
                        scores -> scores.refreshing(),
                        scores -> scores.bitterness()
                )
                .containsExactly(100, 75, 50, 25, 0);
        assertThat(response.moodType().shortDescription()).isEqualTo("산뜻한 한 잔이 어울리는 타입");
        assertThat(response.moodType().description()).isEqualTo("오늘은 산뜻한 기분을 즐겨보세요.");
        assertThat(response.moodType().characterQuote()).isEqualTo("기분 좋은 상큼함을 시작해요.");
        assertThat(response.recommendedCocktails()).singleElement().satisfies(item -> {
            assertThat(item.cocktailName()).isEqualTo("모히토");
            assertThat(item.shortDescription()).isEqualTo("상쾌한 민트와 라임의 조화");
            assertThat(item.ranking()).isEqualTo(1);
            assertThat(item.matchScore()).isEqualTo(95);
        });
        assertThat(response.compatibilities().best().name()).isEqualTo("이상주의자");
        assertThat(response.compatibilities().worst().name()).isEqualTo("현실주의자");
    }

    @Test
    void returnsNullWhenWorstCompatibilityIsMissing() {
        MoodTestResult result = org.mockito.Mockito.mock(MoodTestResult.class);
        MoodType moodType = org.mockito.Mockito.mock(MoodType.class);
        MoodType bestMoodType = org.mockito.Mockito.mock(MoodType.class);
        MoodTypeCompatibility bestCompatibility = org.mockito.Mockito.mock(
                MoodTypeCompatibility.class
        );

        when(result.getMoodType()).thenReturn(moodType);
        when(result.toTasteProfile()).thenReturn(tasteProfile("3.0", "3.0", "3.0", "3.0", "3.0"));
        when(moodType.getId()).thenReturn(3L);
        when(moodType.toTasteProfile()).thenReturn(tasteProfile("3.0", "3.0", "3.0", "3.0", "3.0"));
        when(bestCompatibility.getCompatibilityType()).thenReturn(CompatibilityType.BEST);
        when(bestCompatibility.getTargetMoodType()).thenReturn(bestMoodType);
        when(bestMoodType.getId()).thenReturn(4L);
        when(bestMoodType.getCode()).thenReturn("BEST_TYPE");
        when(bestMoodType.getName()).thenReturn("이상주의자");
        when(moodTestResultRepository.findDetailByIdAndUserId(10L, USER_ID))
                .thenReturn(Optional.of(result));
        when(recommendationRepository.findByTestResult(
                USER_ID,
                10L,
                RecommendationSessionType.TEST_RESULT
        )).thenReturn(List.of());
        when(moodTypeCompatibilityRepository.findAllByMoodTypeId(3L))
                .thenReturn(List.of(bestCompatibility));

        var response = historyService.getTestResultDetail(USER_ID, 10L);

        assertThat(response.compatibilities().best().name()).isEqualTo("이상주의자");
        assertThat(response.compatibilities().worst()).isNull();
    }

    @Test
    void rejectsNonPositiveResourceIdBeforeQueryingRepository() {
        assertThatThrownBy(() -> historyService.getDetail(USER_ID, 0L))
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("HISTORY400"));

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

    private HistoryRepository.MonthlyDrinkingRecordSummary monthlyRecord(
            LocalDate recordDate,
            Long recordId,
            Long cocktailId,
            String cocktailName
    ) {
        HistoryRepository.MonthlyDrinkingRecordSummary record = org.mockito.Mockito.mock(
                HistoryRepository.MonthlyDrinkingRecordSummary.class
        );
        when(record.getRecordDate()).thenReturn(recordDate);
        when(record.getRecordId()).thenReturn(recordId);
        when(record.getCocktailId()).thenReturn(cocktailId);
        when(record.getCocktailName()).thenReturn(cocktailName);
        return record;
    }

    private TasteProfile tasteProfile(
            String alcoholIntensity,
            String sweetness,
            String sourness,
            String refreshing,
            String bitterness
    ) {
        return TasteProfile.of(
                new BigDecimal(alcoholIntensity),
                new BigDecimal(sweetness),
                new BigDecimal(sourness),
                new BigDecimal(refreshing),
                new BigDecimal(bitterness)
        );
    }
}
