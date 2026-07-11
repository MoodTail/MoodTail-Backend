package com.example.moodtail.domain.cocktail.dto.response;

import com.example.moodtail.domain.image.entity.Image;
import com.example.moodtail.domain.cocktail.entity.Cocktail;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record CocktailListResponse(
        List<CocktailSummaryDto> cocktails
) {
    @Builder
    public record CocktailSummaryDto(
            Long cocktailId,
            String nameKo,
            String nameEn,
            BigDecimal alcoholDegree,
            String description,
            String imageUrl,
            @JsonProperty("isFavorite") boolean isFavorite
    ) {

        public static CocktailSummaryDto from(
                Cocktail cocktail,
                boolean isFavorite
        ) {
            return CocktailSummaryDto.builder()
                    .cocktailId(cocktail.getId())
                    .nameKo(cocktail.getNameKo())
                    .nameEn(cocktail.getNameEn())
                    .alcoholDegree(cocktail.getAlcoholDegree())
                    .description(cocktail.getShortDescription())
                    .imageUrl(getImageUrl(cocktail.getImage()))
                    .isFavorite(isFavorite)
                    .build();
        }

        private static String getImageUrl(Image image) {
            return image == null ? null : image.getImageUrl();
        }
    }
}
