package com.example.moodtail.domain.moodtest.service;

import com.example.moodtail.domain.moodtest.dto.response.MoodTestQuestionResponse;
import com.example.moodtail.domain.moodtest.entity.MoodQuestion;
import com.example.moodtail.domain.moodtest.entity.MoodQuestionType;
import com.example.moodtail.domain.moodtest.repository.MoodQuestionRepository;
import com.example.moodtail.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.moodtail.global.common.exception.code.status.MoodTestErrorStatus.MOOD_TEST_QUESTION_QUERY_FAILED;

@Service
@RequiredArgsConstructor
public class MoodTestQuestionService {

    private static final int FIXED_QUESTION_COUNT = 5;
    private static final int RANDOM_QUESTION_COUNT = 2;

    private final MoodQuestionRepository moodQuestionRepository;

    @Transactional(readOnly = true)
    public MoodTestQuestionResponse getQuestions() {
        List<MoodQuestion> fixedQuestions = moodQuestionRepository
                .findDistinctByQuestionTypeAndIsActiveTrueOrderBySortOrderAsc(MoodQuestionType.FIXED);
        validateFixedQuestions(fixedQuestions);

        List<MoodQuestion> randomQuestions = moodQuestionRepository
                .findDistinctByQuestionTypeAndIsActiveTrueOrderBySortOrderAsc(MoodQuestionType.RANDOM);
        validateRandomQuestions(randomQuestions);

        List<MoodQuestion> selectedRandomQuestions = pickRandomQuestions(randomQuestions);
        List<MoodQuestion> questions = new ArrayList<>(FIXED_QUESTION_COUNT + RANDOM_QUESTION_COUNT);
        questions.addAll(fixedQuestions);
        questions.addAll(selectedRandomQuestions);

        return MoodTestQuestionResponse.from(questions);
    }

    private void validateFixedQuestions(List<MoodQuestion> fixedQuestions) {
        if (fixedQuestions.size() != FIXED_QUESTION_COUNT) {
            throw new RestApiException(MOOD_TEST_QUESTION_QUERY_FAILED);
        }
    }

    private void validateRandomQuestions(List<MoodQuestion> randomQuestions) {
        if (randomQuestions.size() < RANDOM_QUESTION_COUNT) {
            throw new RestApiException(MOOD_TEST_QUESTION_QUERY_FAILED);
        }
    }

    private List<MoodQuestion> pickRandomQuestions(List<MoodQuestion> randomQuestions) {
        List<MoodQuestion> shuffledQuestions = new ArrayList<>(randomQuestions);
        Collections.shuffle(shuffledQuestions);
        return shuffledQuestions.subList(0, RANDOM_QUESTION_COUNT);
    }
}
