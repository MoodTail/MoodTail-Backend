package com.example.moodtail.domain.recommendation.dto.response;

import com.example.moodtail.domain.cocktail.dto.response.CocktailDetailResponse;
import com.example.moodtail.domain.cocktail.entity.Cocktail;
import io.swagger.v3.oas.annotations.media.Schema;

public record RecommendedCocktailResponse(
        @Schema(description = "추천 순위. 1~4위입니다.", example = "1" ,minimum = "1", maximum = "4")
        int ranking,

        @Schema(description = "칵테일 ID", example = "18")
        Long cocktailId,

        @Schema(description = "칵테일 한국 이름", example = "프렌치 75")
        String nameKo,

        @Schema(description = "칵테일 영문 이름", example = "French 75")
        String nameEn,

        @Schema(description = "일치율. 두 사용자의 타협 프로필 기준입니다. 0~100 점수입니다.", example = "93", minimum = "0", maximum = "100")
        int matchScore,

        @Schema(description = "나의 개별 취향 프로필 기준 일치율. 0~100 점수입니다.", example = "90", minimum = "0", maximum = "100")
        int myMatchScore,

        @Schema(description = "상대방의 개별 취향 프로필 기준 일치율. 0~100 점수입니다.", example = "85", minimum = "0", maximum = "100")
        int partnerMatchScore
){
    public static RecommendedCocktailResponse of(
            Cocktail cocktail,
            int ranking,
            int matchScore,
            int myMatchScore,
            int partnerMatchScore
    ) {
        return new RecommendedCocktailResponse(
                ranking,
                cocktail.getId(),
                cocktail.getNameKo(),
                cocktail.getNameEn(),
                matchScore,
                myMatchScore,
                partnerMatchScore
        );

    }
}
