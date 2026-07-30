package com.example.moodtail.domain.recommendation.dto.response;

public record PairRecommendationSharePageResponse(
        String shareUrl,
        String frontendUrl,
        String thumbnailImageUrl
) {
}
