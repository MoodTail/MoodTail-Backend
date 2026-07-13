package com.example.moodtail.domain.cocktail.dto.response;

import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.example.moodtail.domain.image.entity.Image;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record CocktailFavoriteListResponse(
        List<CocktailFavoriteSummaryDto> cocktails
) {
    @Builder
    public record CocktailFavoriteSummaryDto(
            Long cocktailId,
            String name,
            String description,
            String imageUrl,
            @JsonProperty("isFavorite") boolean isFavorite
    ) {

        public static CocktailFavoriteSummaryDto from(Cocktail cocktail) {
            return CocktailFavoriteSummaryDto.builder()
                    .cocktailId(cocktail.getId())
                    .name(cocktail.getNameKo())
                    .description(cocktail.getShortDescription())
                    .imageUrl(getImageUrl(cocktail.getImage()))
                    .isFavorite(true)
                    .build();
        }

        private static String getImageUrl(Image image) {
            return image == null ? null : image.getImageUrl();
        }
    }
}
