package com.example.moodtail.domain.history.service;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.cocktail.repository.CocktailRepository;
import com.example.moodtail.domain.history.dto.request.HistoryCreateRequest;
import com.example.moodtail.domain.history.dto.request.HistoryUpdateRequest;
import com.example.moodtail.domain.history.dto.response.HistoryCalendarResponse;
import com.example.moodtail.domain.history.dto.response.HistoryCreateResponse;
import com.example.moodtail.domain.history.dto.response.HistoryDateResponse;
import com.example.moodtail.domain.history.dto.response.HistoryDetailResponse;
import com.example.moodtail.domain.history.dto.response.HistoryTestResultDetailResponse;
import com.example.moodtail.domain.history.dto.response.HistoryUpdateResponse;
import com.example.moodtail.domain.history.entity.DrinkingRecord;
import com.example.moodtail.domain.history.entity.HistoryPhoto;
import com.example.moodtail.domain.history.repository.HistoryMoodTestResultRepository;
import com.example.moodtail.domain.history.repository.HistoryPhotoRepository;
import com.example.moodtail.domain.history.repository.HistoryRecommendationRepository;
import com.example.moodtail.domain.history.repository.HistoryRepository;
import com.example.moodtail.domain.image.entity.Image;
import com.example.moodtail.domain.moodtest.entity.MoodTestResult;
import com.example.moodtail.domain.moodtest.entity.MoodType;
import com.example.moodtail.domain.recommendation.entity.RecommendationItem;
import com.example.moodtail.domain.recommendation.entity.RecommendationSessionType;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.repository.UserRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.example.moodtail.global.common.exception.code.status.CocktailErrorStatus.COCKTAIL_NOT_FOUND;
import static com.example.moodtail.global.common.exception.code.status.AuthErrorStatus.USER_NOT_FOUND;
import static com.example.moodtail.global.common.exception.code.status.HistoryErrorStatus.DRINKING_RECORD_ALREADY_EXISTS;
import static com.example.moodtail.global.common.exception.code.status.HistoryErrorStatus.HISTORY_NOT_FOUND;
import static com.example.moodtail.global.common.exception.code.status.HistoryErrorStatus.INVALID_REQUEST;
import static com.example.moodtail.global.common.exception.code.status.HistoryErrorStatus.TEST_RESULT_NOT_FOUND;
import static com.example.moodtail.global.common.exception.code.status.GlobalErrorStatus._INTERNAL_SERVER_ERROR;

@Service
@Slf4j
@RequiredArgsConstructor
public class HistoryService {

    private static final int MONTHLY_REPORT_REQUIRED_TEST_COUNT = 5;
    private static final String USER_DATE_COCKTAIL_UNIQUE_CONSTRAINT =
            "uk_drinking_record_user_date_cocktail";

    private final HistoryRepository historyRepository;
    private final HistoryPhotoRepository historyPhotoRepository;
    private final HistoryMoodTestResultRepository moodTestResultRepository;
    private final HistoryRecommendationRepository recommendationRepository;
    private final CocktailRepository cocktailRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public HistoryCalendarResponse getCalendar(Long userId, int year, int month) {
        LocalDate today = LocalDate.now(clock);
        YearMonth requestedMonth = HistoryDatePolicy.parseYearMonth(year, month);
        if (requestedMonth.isAfter(YearMonth.from(today))) {
            throw new RestApiException(INVALID_REQUEST);
        }

        LocalDate startDate = requestedMonth.atDay(1);
        LocalDate endDate = requestedMonth.equals(YearMonth.from(today))
                ? today
                : requestedMonth.atEndOfMonth();
        List<MoodTestResult> testResults = moodTestResultRepository.findAllWithMoodType(
                userId,
                startDate,
                endDate
        );
        Set<LocalDate> drinkingRecordDates = new TreeSet<>(historyRepository.findRecordDates(
                userId,
                startDate,
                endDate
        ));
        long drinkingRecordCount = historyRepository.countByUserIdAndRecordDateBetween(
                userId,
                startDate,
                endDate
        );

        Map<LocalDate, MoodTestResult> testResultByDate = new HashMap<>();
        testResults.forEach(result -> testResultByDate.putIfAbsent(result.getResultDate(), result));

        Set<LocalDate> activeDates = new TreeSet<>();
        activeDates.addAll(testResultByDate.keySet());
        activeDates.addAll(drinkingRecordDates);

        List<HistoryCalendarResponse.Day> days = activeDates.stream()
                .map(date -> {
                    MoodTestResult result = testResultByDate.get(date);
                    return new HistoryCalendarResponse.Day(
                            date,
                            result != null,
                            drinkingRecordDates.contains(date),
                            result == null ? null : toCalendarMoodType(result.getMoodType())
                    );
                })
                .toList();

        List<HistoryCalendarResponse.MonthlyTestResult> monthlyResults = testResults.stream()
                .map(result -> new HistoryCalendarResponse.MonthlyTestResult(
                        result.getId(),
                        result.getResultDate(),
                        toCalendarMoodType(result.getMoodType())
                ))
                .toList();

        boolean reportAvailable = testResults.size() >= MONTHLY_REPORT_REQUIRED_TEST_COUNT;

        return new HistoryCalendarResponse(
                requestedMonth.getYear(),
                requestedMonth.getMonthValue(),
                testResults.size(),
                drinkingRecordCount,
                MONTHLY_REPORT_REQUIRED_TEST_COUNT,
                reportAvailable,
                monthlyResults,
                days
        );
    }

    @Transactional(readOnly = true)
    public HistoryDateResponse getByDate(Long userId, String dateValue) {
        LocalDate date = HistoryDatePolicy.parse(dateValue);
        HistoryDatePolicy.validateRecordDate(date, LocalDate.now(clock));

        MoodTestResult testResult = moodTestResultRepository
                .findWithMoodTypeByUserIdAndResultDate(userId, date)
                .orElse(null);
        List<DrinkingRecord> records = historyRepository
                .findAllWithDetailsByUserIdAndRecordDate(userId, date);
        List<HistoryPhoto> photos = historyPhotoRepository.findAllByUserIdAndRecordDate(userId, date);

        return new HistoryDateResponse(
                date,
                testResult == null ? null : new HistoryDateResponse.TestResult(
                        testResult.getId(),
                        toDateMoodType(testResult.getMoodType())
                ),
                records.stream().map(this::toDateRecord).toList(),
                photos.stream().map(this::toPhoto).toList()
        );
    }

    @Transactional(readOnly = true)
    public HistoryTestResultDetailResponse getTestResultDetail(Long userId, Long resultId) {
        validateId(resultId);
        MoodTestResult result = moodTestResultRepository.findDetailByIdAndUserId(resultId, userId)
                .orElseThrow(() -> new RestApiException(TEST_RESULT_NOT_FOUND));
        List<RecommendationItem> recommendations = recommendationRepository.findByTestResult(
                userId,
                resultId,
                RecommendationSessionType.TEST_RESULT
        );

        MoodType moodType = result.getMoodType();
        return new HistoryTestResultDetailResponse(
                result.getId(),
                result.getResultDate(),
                new HistoryTestResultDetailResponse.MoodType(
                        moodType.getId(),
                        moodType.getCode(),
                        moodType.getName(),
                        moodType.getShortDescription(),
                        moodType.getDescription(),
                        moodType.getCharacterQuote(),
                        imageUrl(moodType.getCharacterImage())
                ),
                new HistoryTestResultDetailResponse.TasteProfile(
                        result.getAlcoholIntensity(),
                        result.getSweetness(),
                        result.getSourness(),
                        result.getRefreshing(),
                        result.getBitterness()
                ),
                recommendations.stream().map(this::toRecommendedCocktail).toList()
        );
    }

    @Transactional(readOnly = true)
    public HistoryDetailResponse getDetail(Long userId, Long recordId) {
        validateId(recordId);
        DrinkingRecord record = historyRepository.findWithDetailsByIdAndUserId(recordId, userId)
                .orElseThrow(() -> new RestApiException(HISTORY_NOT_FOUND));
        return toDetail(record);
    }

    @Transactional
    public HistoryCreateResponse create(Long userId, HistoryCreateRequest request) {
        LocalDateTime now = LocalDateTime.now(clock);
        HistoryDatePolicy.validateRecordDate(request.recordDate(), now.toLocalDate());
        if (historyRepository.existsByUserIdAndRecordDateAndCocktailId(
                userId,
                request.recordDate(),
                request.cocktailId()
        )) {
            throw new RestApiException(DRINKING_RECORD_ALREADY_EXISTS);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(USER_NOT_FOUND));
        Cocktail cocktail = cocktailRepository.findById(request.cocktailId())
                .orElseThrow(() -> new RestApiException(COCKTAIL_NOT_FOUND));

        DrinkingRecord record;
        try {
            record = historyRepository.saveAndFlush(DrinkingRecord.create(
                    user,
                    cocktail,
                    request.recordDate(),
                    now
            ));
        } catch (DataIntegrityViolationException exception) {
            throw translateWriteFailure(exception);
        }
        return new HistoryCreateResponse(record.getId(), record.getRecordDate());
    }

    @Transactional
    public HistoryUpdateResponse update(Long userId, Long recordId, HistoryUpdateRequest request) {
        validateId(recordId);
        if (request.cocktailId() == null && request.recordDate() == null) {
            throw new RestApiException(INVALID_REQUEST);
        }

        DrinkingRecord record = historyRepository.findOwnedForUpdate(recordId, userId)
                .orElseThrow(() -> new RestApiException(HISTORY_NOT_FOUND));
        LocalDate targetDate = request.recordDate() == null
                ? record.getRecordDate()
                : request.recordDate();
        if (request.recordDate() != null) {
            HistoryDatePolicy.validateRecordDate(targetDate, LocalDate.now(clock));
        }
        Cocktail targetCocktail = request.cocktailId() == null
                ? record.getCocktail()
                : cocktailRepository.findById(request.cocktailId())
                .orElseThrow(() -> new RestApiException(COCKTAIL_NOT_FOUND));

        if (historyRepository.existsByUserIdAndRecordDateAndCocktailIdAndIdNot(
                userId,
                targetDate,
                targetCocktail.getId(),
                recordId
        )) {
            throw new RestApiException(DRINKING_RECORD_ALREADY_EXISTS);
        }
        record.updateRecordDate(targetDate);
        record.updateCocktail(targetCocktail);
        try {
            historyRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw translateWriteFailure(exception);
        }
        return new HistoryUpdateResponse(record.getId());
    }

    @Transactional
    public void delete(Long userId, Long recordId) {
        validateId(recordId);
        DrinkingRecord record = historyRepository.findOwnedForUpdate(recordId, userId)
                .orElseThrow(() -> new RestApiException(HISTORY_NOT_FOUND));

        historyRepository.delete(record);
        historyRepository.flush();
    }

    private void validateId(Long id) {
        if (id == null || id < 1) {
            throw new RestApiException(INVALID_REQUEST);
        }
    }

    private RuntimeException translateWriteFailure(DataIntegrityViolationException exception) {
        Throwable current = exception;
        while (current != null) {
            String message = current.getMessage();
            if (message != null
                    && message.toLowerCase().contains(USER_DATE_COCKTAIL_UNIQUE_CONSTRAINT)) {
                return new RestApiException(DRINKING_RECORD_ALREADY_EXISTS);
            }
            current = current.getCause();
        }
        log.error("Failed to persist drinking record", exception);
        return new RestApiException(_INTERNAL_SERVER_ERROR);
    }

    private HistoryCalendarResponse.MoodType toCalendarMoodType(MoodType moodType) {
        return new HistoryCalendarResponse.MoodType(
                moodType.getId(),
                moodType.getCode(),
                moodType.getName(),
                imageUrl(moodType.getCharacterImage())
        );
    }

    private HistoryDateResponse.MoodType toDateMoodType(MoodType moodType) {
        return new HistoryDateResponse.MoodType(
                moodType.getId(),
                moodType.getCode(),
                moodType.getName(),
                moodType.getShortDescription(),
                imageUrl(moodType.getCharacterImage())
        );
    }

    private HistoryDateResponse.DrinkingRecordItem toDateRecord(DrinkingRecord record) {
        return new HistoryDateResponse.DrinkingRecordItem(
                record.getId(),
                record.getCocktail().getId(),
                record.getCocktail().getNameKo(),
                imageUrl(record.getCocktail().getImage())
        );
    }

    private HistoryDetailResponse toDetail(DrinkingRecord record) {
        return new HistoryDetailResponse(
                record.getId(),
                record.getCocktail().getId(),
                record.getCocktail().getNameKo(),
                imageUrl(record.getCocktail().getImage()),
                record.getRecordDate()
        );
    }

    private HistoryDateResponse.Photo toPhoto(HistoryPhoto photo) {
        return new HistoryDateResponse.Photo(
                photo.getId(),
                photo.getImage().getSourceType(),
                photo.getImage().getImageUrl()
        );
    }

    private HistoryTestResultDetailResponse.RecommendedCocktail toRecommendedCocktail(RecommendationItem item) {
        Cocktail cocktail = item.getCocktail();
        return new HistoryTestResultDetailResponse.RecommendedCocktail(
                cocktail.getId(),
                cocktail.getNameKo(),
                imageUrl(cocktail.getImage()),
                item.getRanking(),
                item.getMatchScore()
        );
    }

    private String imageUrl(Image image) {
        return image == null ? null : image.getImageUrl();
    }

}
