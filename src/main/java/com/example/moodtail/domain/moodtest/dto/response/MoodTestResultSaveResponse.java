package com.example.moodtail.domain.moodtest.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MoodTestResultSaveResponse(
        @JsonProperty("test_result_id")
        Long testResultId
) {
}
