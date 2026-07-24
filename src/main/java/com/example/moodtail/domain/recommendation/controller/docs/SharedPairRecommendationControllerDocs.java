package com.example.moodtail.domain.recommendation.controller.docs;

import com.example.moodtail.domain.recommendation.dto.response.PairRecommendationShareResultResponse;
import com.example.moodtail.global.common.base.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Pair Recommendation Shares", description = "페어 추천 결과 공유 URL 생성 및 공개 결과 조회 API")
public interface SharedPairRecommendationControllerDocs {

    @Operation(
            operationId = "getSharedPairRecommendation",
            summary = "공유된 페어 추천 결과 조회"
    )
    BaseResponse<PairRecommendationShareResultResponse> getSharedPairRecommendation(
            String token
    );
}
