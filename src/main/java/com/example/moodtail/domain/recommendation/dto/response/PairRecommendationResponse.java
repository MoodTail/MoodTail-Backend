package com.example.moodtail.domain.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PairRecommendationResponse(
        @Schema(description = "추천 결과 저장 여부. 공유 토큰 방식(비로그인)에서는 false입니다.", example = "true")
        boolean recommendationSaved,

        @Schema(description = "두 사용자의 평균 취향 프로필")
        CompromiseProfileResponse compromiseProfile,

        @Schema(description = "추천 칵테일 목록. 일치율 상위 4종입니다.")
        List<RecommendedCocktailResponse> recommendations
) {
}
