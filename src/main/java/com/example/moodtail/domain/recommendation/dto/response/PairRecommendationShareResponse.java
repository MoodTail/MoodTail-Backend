package com.example.moodtail.domain.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PairRecommendationShareResponse(
        @Schema(description = "공유 토큰", example = "psh_9f3ab21")
        String shareToken,

        @Schema(description = "공유 URL", example = "https://moodtail.com/share/pair/psh_9f3ab21")
        String shareUrl
) {
}
