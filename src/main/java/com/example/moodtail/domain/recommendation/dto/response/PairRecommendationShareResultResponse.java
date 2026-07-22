package com.example.moodtail.domain.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PairRecommendationShareResultResponse(
        @Schema(description = "타협 취향 프로필")
        CompromiseProfileResponse compromiseProfile,

        @Schema(description = "추천 칵테일 목록")
        List<SharedRecommendedCocktailResponse> recommendations,

        @Schema(description = "나의 개별 취향 프로필 기준 일치율. 0~100 점수입니다.", example = "90", minimum = "0", maximum = "100")
        Integer myMatchScore,

        @Schema(description = "상대방의 개별 취향 프로필 기준 일치율. 0~100 점수입니다.", example = "85", minimum = "0", maximum = "100")
        Integer partnerMatchScore,

        @Schema(description = "공유 썸네일 이미지 URL")
        String thumbnailImageUrl
) {
    public static PairRecommendationShareResultResponse of(
            CompromiseProfileResponse compromiseProfile,
            List<SharedRecommendedCocktailResponse> recommendations,
            Integer myMatchScore,
            Integer partnerMatchScore,
            String thumbnailImageUrl
    ) {
        return new PairRecommendationShareResultResponse(
                compromiseProfile,
                recommendations,
                myMatchScore,
                partnerMatchScore,
                thumbnailImageUrl
        );
    }
}
