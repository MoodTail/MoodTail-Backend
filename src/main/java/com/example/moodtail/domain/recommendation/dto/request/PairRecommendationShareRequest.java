package com.example.moodtail.domain.recommendation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record PairRecommendationShareRequest(
        @NotNull(message = "타협 취향 프로필은 필수입니다.")
        @Valid
        CompromiseProfileDto compromiseProfile,

        @NotNull(message = "추천 칵테일 목록은 필수입니다.")
        @Valid
        List<RecommendationDto> recommendations,

        @NotNull(message = "내 일치율은 필수입니다.")
        @Min(value = 0, message = "내 일치율은 0 이상이어야 합니다.")
        @Max(value = 100, message = "내 일치율은 100 이하여야 합니다.")
        Integer myMatchScore,

        @NotNull(message = "상대방 일치율은 필수입니다.")
        @Min(value = 0, message = "상대방 일치율은 0 이상이어야 합니다.")
        @Max(value = 100, message = "상대방 일치율은 100 이하여야 합니다.")
        Integer partnerMatchScore,

        @NotBlank(message = "썸네일 이미지 URL은 필수입니다.")
        String thumbnailImageUrl
) {

    public record CompromiseProfileDto(
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

    public record RecommendationDto(
            @NotNull(message = "추천 순위는 필수입니다.")
            @Min(value = 1, message = "추천 순위는 1 이상이어야 합니다.")
            @Max(value = 3, message = "추천 순위는 3 이하여야 합니다.")
            Integer ranking,

            @NotNull(message = "칵테일 ID는 필수입니다.")
            Long cocktailId,

            @NotNull(message = "일치율은 필수입니다.")
            @Min(value = 0, message = "일치율은 0 이상이어야 합니다.")
            @Max(value = 100, message = "일치율은 100 이하여야 합니다.")
            Integer matchScore
    ) {
    }
}
