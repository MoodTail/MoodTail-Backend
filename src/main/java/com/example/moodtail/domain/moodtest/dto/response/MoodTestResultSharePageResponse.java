package com.example.moodtail.domain.moodtest.dto.response;

public record MoodTestResultSharePageResponse(
        String shareUrl,
        String frontendUrl,
        String thumbnailImageUrl
) {
}
