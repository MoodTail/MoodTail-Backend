package com.example.moodtail.domain.recommendation.dto.response;

import com.example.moodtail.domain.recommendation.model.DominantSide;
import com.example.moodtail.domain.recommendation.model.TasteMetricContribution;
import io.swagger.v3.oas.annotations.media.Schema;

public record TasteContributionResponse(
        @Schema(description = "기여 지표. alcoholIntensity/sweetness/sourness/refreshing/bitterness 중 하나입니다.", example = "sweetness")
        String metric,

        @Schema(description = "기여 지표의 한글명", example = "달콤함")
        String metricNameKo,

        @Schema(description = "해당 지표에 취향이 더 많이 반영된 쪽. ME 또는 PARTNER입니다.", example = "ME")
        DominantSide dominantSide
) {
    public static TasteContributionResponse from(TasteMetricContribution contribution) {
        return new TasteContributionResponse(
                contribution.metric(),
                contribution.metricNameKo(),
                contribution.dominantSide()
        );
    }
}
