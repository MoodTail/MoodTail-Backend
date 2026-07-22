package com.example.moodtail.domain.recommendation.dto.response;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
import io.swagger.v3.oas.annotations.media.Schema;

public record SharedRecommendedCocktailResponse(
        @Schema(description = "추천 순위. 1~3위입니다.", example = "1", minimum = "1", maximum = "3")
        int ranking,

        @Schema(description = "칵테일 ID", example = "18")
        Long cocktailId,

        @Schema(description = "칵테일 한국 이름", example = "프렌치 75")
        String nameKo,

        @Schema(description = "칵테일 영문 이름", example = "French 75")
        String nameEn,

        @Schema(description = "일치율. 두 사용자의 타협 프로필 기준입니다. 0~100 점수입니다.", example = "93", minimum = "0", maximum = "100")
        int matchScore
) {
    public static SharedRecommendedCocktailResponse of(
            Cocktail cocktail,
            int ranking,
            int matchScore
    ) {
        return new SharedRecommendedCocktailResponse(
                ranking,
                cocktail.getId(),
                cocktail.getNameKo(),
                cocktail.getNameEn(),
                matchScore
        );
    }
}
