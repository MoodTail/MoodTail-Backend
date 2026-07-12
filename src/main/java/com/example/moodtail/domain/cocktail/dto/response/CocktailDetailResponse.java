package com.example.moodtail.domain.cocktail.dto.response;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.cocktail.entity.CocktailIngredient;
import com.example.moodtail.domain.cocktail.entity.CocktailRecipeStep;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

public record CocktailDetailResponse(

        @Schema(description = "칵테일 ID", example = "15")
        Long cocktailId,

        @Schema(description = "칵테일 이름", example = "모히또")
        String name,

        @Schema(description = "한줄 설명", example = "가볍고 청량한 럼 베이스 칵테일")
        String shortDescription,

        @Schema(description = "이미지 URL")
        String imageUrl,

        @Schema(description = "도수 지표. 1~5점 척도입니다.", example = "2.0", minimum = "1", maximum = "5")
        BigDecimal alcoholIntensity,

        @Schema(description = "당도 지표. 1~5점 척도입니다.", example = "3.0", minimum = "1", maximum = "5")
        BigDecimal sweetness,

        @Schema(description = "산도 지표. 1~5점 척도입니다.", example = "4.0", minimum = "1", maximum = "5")
        BigDecimal sourness,

        @Schema(description = "청량감 지표. 1~5점 척도입니다.", example = "5.0", minimum = "1", maximum = "5")
        BigDecimal refreshing,

        @Schema(description = "쓴맛 지표. 1~5점 척도입니다.", example = "1.0", minimum = "1", maximum = "5")
        BigDecimal bitterness,

        @Schema(description = "즐겨찾기 여부", example = "false")
        boolean isFavorite,

        List<RecipeStepDto> recipeSteps,

        List<IngredientDto> cocktailIngredients
) {

    public static CocktailDetailResponse from(Cocktail cocktail, boolean isFavorite, String imageUrl) {
        return new CocktailDetailResponse(
                cocktail.getId(),
                cocktail.getNameKo(),
                cocktail.getShortDescription(),
                imageUrl,
                cocktail.getAlcoholIntensity(),
                cocktail.getSweetness(),
                cocktail.getSourness(),
                cocktail.getRefreshing(),
                cocktail.getBitterness(),
                isFavorite,
                cocktail.getRecipeSteps().stream()
                        .map(RecipeStepDto::from)
                        .toList(),
                cocktail.getIngredients().stream()
                        .map(IngredientDto::from)
                        .toList()
        );
    }

    public record RecipeStepDto(

            @Schema(description = "제조 순서", example = "1")
            Integer stepOrder,

            @Schema(description = "제조 설명", example = "라임과 설탕을 넣고 으깬다")
            String description
    ) {
        public static RecipeStepDto from(CocktailRecipeStep step) {
            return new RecipeStepDto(step.getStepOrder(), step.getDescription());
        }
    }

    public record IngredientDto(

            @Schema(description = "재료 이름", example = "화이트 럼")
            String name,

            @Schema(description = "재료 양", example = "50ML")
            String amountText,

            @Schema(description = "정렬 순서", example = "1")
            Integer sortOrder
    ) {
        public static IngredientDto from(CocktailIngredient ingredient) {
            return new IngredientDto(ingredient.getName(), ingredient.getAmountText(), ingredient.getSortOrder());
        }
    }
}