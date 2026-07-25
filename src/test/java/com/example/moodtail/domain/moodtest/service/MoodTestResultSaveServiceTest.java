package com.example.moodtail.domain.moodtest.service;

import com.example.moodtail.domain.collection.entity.UserUnlockedMoodType;
import com.example.moodtail.domain.collection.repository.UserUnlockedMoodTypeRepository;
import com.example.moodtail.domain.moodtest.dto.request.MoodTestResultSaveRequest;
import com.example.moodtail.domain.moodtest.entity.MoodTestResult;
import com.example.moodtail.domain.moodtest.entity.MoodType;
import com.example.moodtail.domain.moodtest.repository.MoodTestResultRepository;
import com.example.moodtail.domain.moodtest.repository.MoodTypeRepository;
import com.example.moodtail.domain.recommendation.service.RecommendationPersistenceService;
import com.example.moodtail.domain.user.entity.User;
import com.example.moodtail.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoodTestResultSaveServiceTest {

    @Mock UserRepository userRepository;
    @Mock MoodTypeRepository moodTypeRepository;
    @Mock MoodTestResultRepository moodTestResultRepository;
    @Mock RecommendationPersistenceService recommendationPersistenceService;
    @Mock UserUnlockedMoodTypeRepository userUnlockedMoodTypeRepository;
    @Mock PlatformTransactionManager transactionManager;

    private MoodTestResultSaveService service;

    @BeforeEach
    void setUp() {
        service = new MoodTestResultSaveService(
                userRepository,
                moodTypeRepository,
                moodTestResultRepository,
                recommendationPersistenceService,
                userUnlockedMoodTypeRepository,
                transactionManager
        );
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
    }

    @Test
    void savingResultUnlocksItsMoodType() {
        User user = member(1L);
        MoodType moodType = moodType(11L, "TYPE_11");
        givenSuccessfulSave(user, moodType, 101L);
        when(userUnlockedMoodTypeRepository.existsByUserIdAndMoodTypeId(1L, 11L)).thenReturn(false);

        Long resultId = service.saveResult(1L, request(11L, "TYPE_11"));

        assertThat(resultId).isEqualTo(101L);
        ArgumentCaptor<UserUnlockedMoodType> captor = ArgumentCaptor.forClass(UserUnlockedMoodType.class);
        verify(userUnlockedMoodTypeRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(user);
        assertThat(captor.getValue().getMoodType()).isSameAs(moodType);
    }

    @Test
    void savingResultDoesNotDuplicateAlreadyUnlockedMoodType() {
        User user = member(2L);
        MoodType moodType = moodType(12L, "TYPE_12");
        givenSuccessfulSave(user, moodType, 102L);
        when(userUnlockedMoodTypeRepository.existsByUserIdAndMoodTypeId(2L, 12L)).thenReturn(true);

        service.saveResult(2L, request(12L, "TYPE_12"));

        verify(userUnlockedMoodTypeRepository, never()).save(any());
    }

    @Test
    void overwritingTodaysResultUnlocksNewTypeWithoutDeletingPreviousUnlock() {
        User user = member(3L);
        MoodType newMoodType = moodType(13L, "TYPE_13");
        MoodTestResult existingResult = org.mockito.Mockito.mock(MoodTestResult.class);
        MoodTestResult savedResult = savedResult(103L);
        when(userRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(user));
        when(moodTypeRepository.findById(13L)).thenReturn(Optional.of(newMoodType));
        when(moodTestResultRepository.findByUserIdAndResultDate(any(), any()))
                .thenReturn(Optional.of(existingResult));
        when(moodTestResultRepository.saveAndFlush(existingResult)).thenReturn(savedResult);
        when(userUnlockedMoodTypeRepository.existsByUserIdAndMoodTypeId(3L, 13L)).thenReturn(false);

        service.saveResult(3L, request(13L, "TYPE_13"));

        verify(existingResult).updateResult(any(), any());
        verify(userUnlockedMoodTypeRepository).save(any(UserUnlockedMoodType.class));
        verify(userUnlockedMoodTypeRepository, never()).delete(any(UserUnlockedMoodType.class));
    }

    @Test
    void guestResultAlsoUnlocksMoodType() {
        User guest = User.createGuest(UUID.randomUUID().toString(), "게스트", LocalDateTime.now());
        ReflectionTestUtils.setField(guest, "id", 4L);
        MoodType moodType = moodType(14L, "TYPE_14");
        givenSuccessfulSave(guest, moodType, 104L);
        when(userUnlockedMoodTypeRepository.existsByUserIdAndMoodTypeId(4L, 14L)).thenReturn(false);

        service.saveResult(4L, request(14L, "TYPE_14"));

        verify(userUnlockedMoodTypeRepository).save(any(UserUnlockedMoodType.class));
    }

    @Test
    void recommendationFailureDoesNotAttemptUnlock() {
        User user = member(5L);
        MoodType moodType = org.mockito.Mockito.mock(MoodType.class);
        when(moodType.getCode()).thenReturn("TYPE_15");
        when(userRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(user));
        when(moodTypeRepository.findById(15L)).thenReturn(Optional.of(moodType));
        when(moodTestResultRepository.findByUserIdAndResultDate(any(), any())).thenReturn(Optional.empty());
        when(moodTestResultRepository.saveAndFlush(any(MoodTestResult.class)))
                .thenReturn(org.mockito.Mockito.mock(MoodTestResult.class));
        doThrow(new IllegalStateException("recommendation save failed"))
                .when(recommendationPersistenceService)
                .replaceTestResultRecommendation(any(), any(), any());

        assertThatThrownBy(() -> service.saveResult(5L, request(15L, "TYPE_15")))
                .isInstanceOf(IllegalStateException.class);

        verify(userUnlockedMoodTypeRepository, never())
                .existsByUserIdAndMoodTypeId(any(), any());
        verify(userUnlockedMoodTypeRepository, never()).save(any());
    }

    private void givenSuccessfulSave(User user, MoodType moodType, Long resultId) {
        MoodTestResult savedResult = savedResult(resultId);
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(moodTypeRepository.findById(moodType.getId())).thenReturn(Optional.of(moodType));
        when(moodTestResultRepository.findByUserIdAndResultDate(any(), any())).thenReturn(Optional.empty());
        when(moodTestResultRepository.saveAndFlush(any(MoodTestResult.class))).thenReturn(savedResult);
    }

    private User member(Long id) {
        User user = User.createMember("사용자", LocalDateTime.now());
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private MoodType moodType(Long id, String code) {
        MoodType moodType = org.mockito.Mockito.mock(MoodType.class);
        when(moodType.getId()).thenReturn(id);
        when(moodType.getCode()).thenReturn(code);
        return moodType;
    }

    private MoodTestResult savedResult(Long id) {
        MoodTestResult result = org.mockito.Mockito.mock(MoodTestResult.class);
        when(result.getId()).thenReturn(id);
        return result;
    }

    private MoodTestResultSaveRequest request(Long moodTypeId, String typeCode) {
        BigDecimal score = new BigDecimal("3.0");
        return new MoodTestResultSaveRequest(
                new MoodTestResultSaveRequest.MoodTypeDto(moodTypeId, typeCode),
                new MoodTestResultSaveRequest.TasteProfileDto(
                        score,
                        score,
                        score,
                        score,
                        score
                ),
                List.of(
                        new MoodTestResultSaveRequest.RecommendedCocktailDto(1L, 90),
                        new MoodTestResultSaveRequest.RecommendedCocktailDto(2L, 80),
                        new MoodTestResultSaveRequest.RecommendedCocktailDto(3L, 70),
                        new MoodTestResultSaveRequest.RecommendedCocktailDto(4L, 60)
                )
        );
    }
}
