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

        @Schema(description = "일치율. 0~100 점수입니다.", example = "93", minimum = "0", maximum = "100")
        int matchScore
){
    public static RecommendedCocktailResponse of(Cocktail cocktail, int ranking, int matchScore) {
        return new RecommendedCocktailResponse(
                ranking,
                cocktail.getId(),
                cocktail.getNameKo(),
                cocktail.getNameEn(),
                matchScore
        );

    }
}
