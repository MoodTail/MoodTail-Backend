package com.example.moodtail.domain.moodtest.dto.response;

import com.example.moodtail.domain.moodtest.entity.MoodQuestion;
import com.example.moodtail.domain.moodtest.entity.MoodQuestionOption;
import com.example.moodtail.domain.moodtest.entity.MoodQuestionType;
import lombok.Builder;

import java.util.List;

@Builder
public record MoodTestQuestionResponse(
        int totalCount,
        List<QuestionDto> questions
) {

    public static MoodTestQuestionResponse from(List<MoodQuestion> questions) {
        return MoodTestQuestionResponse.builder()
                .totalCount(questions.size())
                .questions(questions.stream()
                        .map(QuestionDto::from)
                        .toList())
                .build();
    }

    @Builder
    public record QuestionDto(
            Long questionId,
            MoodQuestionType questionType,
            String content,
            Integer sortOrder,
            List<OptionDto> options
    ) {

        private static QuestionDto from(MoodQuestion question) {
            return QuestionDto.builder()
                    .questionId(question.getId())
                    .questionType(question.getQuestionType())
                    .content(question.getContent())
                    .sortOrder(question.getSortOrder())
                    .options(question.getOptions().stream()
                            .map(OptionDto::from)
                            .toList())
                    .build();
        }
    }

    @Builder
    public record OptionDto(
            Long optionId,
            String content,
            Integer optionOrder
    ) {

        private static OptionDto from(MoodQuestionOption option) {
            return OptionDto.builder()
                    .optionId(option.getId())
                    .content(option.getContent())
                    .optionOrder(option.getOptionOrder())
                    .build();
        }
    }
}
