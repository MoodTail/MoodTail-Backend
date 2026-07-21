package com.example.moodtail.domain.cocktail.dto.response;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
import io.swagger.v3.oas.annotations.media.Schema;

public record CocktailFavoriteResponse(

        @Schema(description = "칵테일 ID", example = "15")
        Long cocktailId,

        @Schema(description = "칵테일 이름", example = "모히또")
        String name
) {

    public static CocktailFavoriteResponse from(Cocktail cocktail) {
        return new CocktailFavoriteResponse(cocktail.getId(), cocktail.getNameKo());
    }
}
