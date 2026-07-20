package com.example.moodtail.domain.cocktail.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CustomCocktailRecommendationRequest(
        @NotNull(message = "도수 점수는 필수입니다.")
        @Min(value = 0, message = "도수 점수는 0 이상이어야 합니다.")
        @Max(value = 100, message = "도수 점수는 100 이하여야 합니다.")
        Integer alcoholIntensity,

        @NotNull(message = "당도 점수는 필수입니다.")
        @Min(value = 0, message = "당도 점수는 0 이상이어야 합니다.")
        @Max(value = 100, message = "당도 점수는 100 이하여야 합니다.")
        Integer sweetness,

        @NotNull(message = "산도 점수는 필수입니다.")
        @Min(value = 0, message = "산도 점수는 0 이상이어야 합니다.")
        @Max(value = 100, message = "산도 점수는 100 이하여야 합니다.")
        Integer sourness,

        @NotNull(message = "청량감 점수는 필수입니다.")
        @Min(value = 0, message = "청량감 점수는 0 이상이어야 합니다.")
        @Max(value = 100, message = "청량감 점수는 100 이하여야 합니다.")
        Integer refreshing,

        @NotNull(message = "쓴맛 점수는 필수입니다.")
        @Min(value = 0, message = "쓴맛 점수는 0 이상이어야 합니다.")
        @Max(value = 100, message = "쓴맛 점수는 100 이하여야 합니다.")
        Integer bitterness
){
}