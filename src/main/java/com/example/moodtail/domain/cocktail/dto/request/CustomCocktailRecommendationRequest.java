package com.example.moodtail.domain.cocktail.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CustomCocktailRecommendationRequest(
        @Schema(
                description = "0 이상 100 이하",
                example = "50",
                minimum = "0",
                maximum = "100",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "도수 점수는 필수입니다.")
        @Min(value = 0, message = "도수 점수는 0 이상이어야 합니다.")
        @Max(value = 100, message = "도수 점수는 100 이하여야 합니다.")
        Integer alcoholIntensity,

        @Schema(
                description = "0 이상 100 이하",
                example = "50",
                minimum = "0",
                maximum = "100",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "당도 점수는 필수입니다.")
        @Min(value = 0, message = "당도 점수는 0 이상이어야 합니다.")
        @Max(value = 100, message = "당도 점수는 100 이하여야 합니다.")
        Integer sweetness,

        @Schema(
                description = "0 이상 100 이하",
                example = "50",
                minimum = "0",
                maximum = "100",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "산도 점수는 필수입니다.")
        @Min(value = 0, message = "산도 점수는 0 이상이어야 합니다.")
        @Max(value = 100, message = "산도 점수는 100 이하여야 합니다.")
        Integer sourness,

        @Schema(
                description = "0 이상 100 이하",
                example = "50",
                minimum = "0",
                maximum = "100",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "청량감 점수는 필수입니다.")
        @Min(value = 0, message = "청량감 점수는 0 이상이어야 합니다.")
        @Max(value = 100, message = "청량감 점수는 100 이하여야 합니다.")
        Integer refreshing,

        @Schema(
                description = "0 이상 100 이하",
                example = "50",
                minimum = "0",
                maximum = "100",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "쓴맛 점수는 필수입니다.")
        @Min(value = 0, message = "쓴맛 점수는 0 이상이어야 합니다.")
        @Max(value = 100, message = "쓴맛 점수는 100 이하여야 합니다.")
        Integer bitterness
){
}