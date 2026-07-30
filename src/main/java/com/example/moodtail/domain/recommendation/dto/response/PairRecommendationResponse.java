package com.example.moodtail.domain.recommendation.dto.response;

import com.example.moodtail.domain.recommendation.model.TasteMetricContribution;
import com.example.moodtail.domain.recommendation.model.TasteProfile;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PairRecommendationResponse(
        @Schema(description = "나(요청자)의 닉네임", example = "사용자")
        String myNickname,

        @Schema(description = "상대방의 닉네임", example = "파트너")
        String partnerNickname,

        @Schema(description = "나의 개별 취향 프로필")
        CompromiseProfileResponse myProfile,

        @Schema(description = "상대방의 개별 취향 프로필")
        CompromiseProfileResponse partnerProfile,

        @Schema(description = "두 사용자의 평균 취향 프로필")
        CompromiseProfileResponse compromiseProfile,

        @Schema(description = "추천 칵테일 목록. 일치율 상위 4종입니다.")
        List<RecommendedCocktailResponse> recommendations,

        @Schema(description = "1위 추천 칵테일의 핵심 지표별 취향 기여도")
        List<TasteContributionResponse> tasteContributions
) {
    public static PairRecommendationResponse of(
            String myNickname,
            String partnerNickname,
            TasteProfile myProfile,
            TasteProfile partnerProfile,
            TasteProfile compromiseProfile,
            List<RecommendedCocktailResponse> recommendations,
            List<TasteMetricContribution> tasteContributions
    ) {
        return new PairRecommendationResponse(
                myNickname,
                partnerNickname,
                CompromiseProfileResponse.from(myProfile),
                CompromiseProfileResponse.from(partnerProfile),
                CompromiseProfileResponse.from(compromiseProfile),
                recommendations,
                tasteContributions.stream().map(TasteContributionResponse::from).toList()
        );
    }
}
