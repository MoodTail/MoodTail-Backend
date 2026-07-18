package com.example.moodtail.domain.moodtest.dto.response;

public record MoodTestResultShareCreateResponse(
        String shareToken,
        String shareUrl
) {
}
