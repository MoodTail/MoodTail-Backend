package com.example.moodtail.domain.moodtest.service;

import com.example.moodtail.domain.moodtest.dto.response.MoodTestQuestionResponse;
import com.example.moodtail.domain.moodtest.entity.MoodQuestion;
import com.example.moodtail.domain.moodtest.entity.MoodQuestionOption;
import com.example.moodtail.domain.moodtest.entity.MoodQuestionType;
import com.example.moodtail.domain.moodtest.repository.MoodQuestionRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoodTestQuestionServiceTest {

    @Mock
    private MoodQuestionRepository moodQuestionRepository;

    @InjectMocks
    private MoodTestQuestionService moodTestQuestionService;

    @Test
    void returnsFiveFixedQuestionsAndTwoRandomQuestions() {
        List<MoodQuestion> fixed = List.of(
                question(1L, MoodQuestionType.FIXED, 1),
                question(2L, MoodQuestionType.FIXED, 2),
                question(3L, MoodQuestionType.FIXED, 3),
                question(4L, MoodQuestionType.FIXED, 4),
                question(5L, MoodQuestionType.FIXED, 5)
        );
        List<MoodQuestion> random = List.of(
                question(11L, MoodQuestionType.RANDOM, 1),
                question(12L, MoodQuestionType.RANDOM, 2),
                question(13L, MoodQuestionType.RANDOM, 3)
        );
        when(moodQuestionRepository
                .findDistinctByQuestionTypeAndIsActiveTrueOrderBySortOrderAsc(MoodQuestionType.FIXED))
                .thenReturn(fixed);
        when(moodQuestionRepository
                .findDistinctByQuestionTypeAndIsActiveTrueOrderBySortOrderAsc(MoodQuestionType.RANDOM))
                .thenReturn(random);

        MoodTestQuestionResponse response = moodTestQuestionService.getQuestions();

        assertThat(response.totalCount()).isEqualTo(7);
        assertThat(response.questions().subList(0, 5))
                .extracting(MoodTestQuestionResponse.QuestionDto::questionId)
                .containsExactly(1L, 2L, 3L, 4L, 5L);
        assertThat(response.questions().subList(5, 7))
                .extracting(MoodTestQuestionResponse.QuestionDto::questionId)
                .isSubsetOf(11L, 12L, 13L);
    }

    @Test
    void rejectsQuestionConfigurationWithoutFiveFixedQuestions() {
        List<MoodQuestion> fixed = List.of(
                question(1L, MoodQuestionType.FIXED, 1),
                question(2L, MoodQuestionType.FIXED, 2)
        );
        when(moodQuestionRepository
                .findDistinctByQuestionTypeAndIsActiveTrueOrderBySortOrderAsc(MoodQuestionType.FIXED))
                .thenReturn(fixed);

        assertThatThrownBy(moodTestQuestionService::getQuestions)
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("MOOD_TEST400")
                );
    }

    @Test
    void rejectsQuestionConfigurationWithLessThanTwoRandomQuestions() {
        List<MoodQuestion> fixed = List.of(
                question(1L, MoodQuestionType.FIXED, 1),
                question(2L, MoodQuestionType.FIXED, 2),
                question(3L, MoodQuestionType.FIXED, 3),
                question(4L, MoodQuestionType.FIXED, 4),
                question(5L, MoodQuestionType.FIXED, 5)
        );
        List<MoodQuestion> random = List.of(
                question(11L, MoodQuestionType.RANDOM, 1)
        );
        when(moodQuestionRepository
                .findDistinctByQuestionTypeAndIsActiveTrueOrderBySortOrderAsc(MoodQuestionType.FIXED))
                .thenReturn(fixed);
        when(moodQuestionRepository
                .findDistinctByQuestionTypeAndIsActiveTrueOrderBySortOrderAsc(MoodQuestionType.RANDOM))
                .thenReturn(random);

        assertThatThrownBy(moodTestQuestionService::getQuestions)
                .isInstanceOfSatisfying(RestApiException.class, exception ->
                        assertThat(exception.getErrorCode().getCode()).isEqualTo("MOOD_TEST400")
                );
    }

    private MoodQuestion question(Long id, MoodQuestionType type, int sortOrder) {
        MoodQuestion question = mock(MoodQuestion.class, withSettings().lenient());
        MoodQuestionOption option = mock(MoodQuestionOption.class, withSettings().lenient());
        when(question.getId()).thenReturn(id);
        when(question.getQuestionType()).thenReturn(type);
        when(question.getContent()).thenReturn("질문 " + id);
        when(question.getSortOrder()).thenReturn(sortOrder);
        when(question.getOptions()).thenReturn(List.of(option));
        when(option.getId()).thenReturn(id * 10);
        when(option.getContent()).thenReturn("선택지 " + id);
        when(option.getOptionOrder()).thenReturn(1);
        return question;
    }
}
