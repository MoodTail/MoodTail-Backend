package com.example.moodtail.domain.recommendation.dto.response;

import com.example.moodtail.domain.recommendation.model.TasteProfile;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record CompromiseProfileResponse(
        @Schema(description = "도수 지표. 1~5점 척도입니다.", example = "3.5", minimum = "1", maximum = "5")
        BigDecimal alcoholIntensity,

        @Schema(description = "당도 지표. 1~5점 척도입니다.", example = "3.8", minimum = "1", maximum = "5")
        BigDecimal sweetness,

        @Schema(description = "산도 지표. 1~5점 척도입니다.", example = "3.2", minimum = "1", maximum = "5")
        BigDecimal sourness,

        @Schema(description = "청량감 지표. 1~5점 척도입니다.", example = "4.0", minimum = "1", maximum = "5")
        BigDecimal refreshing,

        @Schema(description = "쓴맛 지표. 1~5점 척도입니다.", example = "2.1", minimum = "1", maximum = "5")
        BigDecimal bitterness
) {
    public static CompromiseProfileResponse from(TasteProfile profile) {
        return new CompromiseProfileResponse(
                profile.alcoholIntensity(),
                profile.sweetness(),
                profile.sourness(),
                profile.refreshing(),
                profile.bitterness()
        );
    }
}

