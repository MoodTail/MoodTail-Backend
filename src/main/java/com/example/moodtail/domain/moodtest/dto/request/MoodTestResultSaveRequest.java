package com.example.moodtail.domain.moodtest.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record MoodTestResultSaveRequest(
        @NotNull(message = "무드 타입은 필수입니다.")
        @Valid
        MoodTypeDto moodType,

        @NotNull(message = "취향 프로필은 필수입니다.")
        @Valid
        TasteProfileDto tasteProfile,

        @NotEmpty(message = "추천 칵테일은 필수입니다.")
        @Size(min = 4, max = 4, message = "추천 칵테일은 4개여야 합니다.")
        @Valid
        List<RecommendedCocktailDto> recommendedCocktails
) {

    public record MoodTypeDto(
            @JsonAlias("typeId")
            @NotNull(message = "무드 타입 ID는 필수입니다.")
            Long moodTypeId,

            String typeCode
    ) {
    }

    public record TasteProfileDto(
            @NotNull(message = "도수 점수는 필수입니다.")
            @DecimalMin(value = "1.0", message = "도수 점수는 1.0 이상이어야 합니다.")
            @DecimalMax(value = "5.0", message = "도수 점수는 5.0 이하여야 합니다.")
            BigDecimal alcoholIntensity,

            @NotNull(message = "당도 점수는 필수입니다.")
            @DecimalMin(value = "1.0", message = "당도 점수는 1.0 이상이어야 합니다.")
            @DecimalMax(value = "5.0", message = "당도 점수는 5.0 이하여야 합니다.")
            BigDecimal sweetness,

            @NotNull(message = "산도 점수는 필수입니다.")
            @DecimalMin(value = "1.0", message = "산도 점수는 1.0 이상이어야 합니다.")
            @DecimalMax(value = "5.0", message = "산도 점수는 5.0 이하여야 합니다.")
            BigDecimal sourness,

            @NotNull(message = "청량감 점수는 필수입니다.")
            @DecimalMin(value = "1.0", message = "청량감 점수는 1.0 이상이어야 합니다.")
            @DecimalMax(value = "5.0", message = "청량감 점수는 5.0 이하여야 합니다.")
            BigDecimal refreshing,

            @NotNull(message = "쓴맛 점수는 필수입니다.")
            @DecimalMin(value = "1.0", message = "쓴맛 점수는 1.0 이상이어야 합니다.")
            @DecimalMax(value = "5.0", message = "쓴맛 점수는 5.0 이하여야 합니다.")
            BigDecimal bitterness
    ) {
    }

    public record RecommendedCocktailDto(
            @NotNull(message = "칵테일 ID는 필수입니다.")
            Long cocktailId,

            @JsonAlias("matchRate")
            @NotNull(message = "일치율은 필수입니다.")
            @Min(value = 0, message = "일치율은 0 이상이어야 합니다.")
            @Max(value = 100, message = "일치율은 100 이하여야 합니다.")
            Integer matchScore
    ) {
    }
}
