package com.example.moodtail.domain.moodtest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MoodTestResultShareCreateRequest(
        @NotNull(message = "취향 프로필은 필수입니다.")
        @Valid
        TasteProfileDto tasteProfile
) {

    public record TasteProfileDto(
            @NotNull(message = "알코올 강도는 필수입니다.")
            @DecimalMin(value = "1.0", message = "알코올 강도는 1.0 이상이어야 합니다.")
            @DecimalMax(value = "5.0", message = "알코올 강도는 5.0 이하여야 합니다.")
            BigDecimal alcoholIntensity,

            @NotNull(message = "단맛은 필수입니다.")
            @DecimalMin(value = "1.0", message = "단맛은 1.0 이상이어야 합니다.")
            @DecimalMax(value = "5.0", message = "단맛은 5.0 이하여야 합니다.")
            BigDecimal sweetness,

            @NotNull(message = "신맛은 필수입니다.")
            @DecimalMin(value = "1.0", message = "신맛은 1.0 이상이어야 합니다.")
            @DecimalMax(value = "5.0", message = "신맛은 5.0 이하여야 합니다.")
            BigDecimal sourness,

            @NotNull(message = "청량감은 필수입니다.")
            @DecimalMin(value = "1.0", message = "청량감은 1.0 이상이어야 합니다.")
            @DecimalMax(value = "5.0", message = "청량감은 5.0 이하여야 합니다.")
            BigDecimal refreshing,

            @NotNull(message = "쓴맛은 필수입니다.")
            @DecimalMin(value = "1.0", message = "쓴맛은 1.0 이상이어야 합니다.")
            @DecimalMax(value = "5.0", message = "쓴맛은 5.0 이하여야 합니다.")
            BigDecimal bitterness
    ) {
    }
}
