package com.example.moodtail.domain.cocktail.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record DailyCocktailRequest(
        @Schema(
                description = "위도, -90 이상 90 이하",
                example = "37.5665",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "위도는 필수입니다.")
        @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
        @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
        Double latitude,

        @Schema(
                description = "경도, -180 이상 180 이하",
                example = "126.9780",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "경도는 필수입니다.")
        @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
        @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다.")
        Double longitude
) {
}
