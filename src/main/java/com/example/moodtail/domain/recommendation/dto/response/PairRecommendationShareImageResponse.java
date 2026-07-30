package com.example.moodtail.domain.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PairRecommendationShareImageResponse(
        @Schema(description = "S3에 저장된 페어 추천 공유 이미지 URL", example = "https://moodtail-bucket.s3.ap-northeast-2.amazonaws.com/recommendations/pair/9f3ab21c-....png")
        String shareImageUrl
) {
}
