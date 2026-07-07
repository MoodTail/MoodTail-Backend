package com.example.moodtail.domain.moodtest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MoodTestResultRequest(
        @NotEmpty(message = "답변은 필수입니다.")
        @Valid
        List<AnswerDto> answers
) {

    public record AnswerDto(
            @NotNull(message = "문항 ID는 필수입니다.")
            Long questionId,

            @NotNull(message = "선택지 ID는 필수입니다.")
            Long optionId
    ) {
    }
}
