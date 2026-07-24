package com.example.moodtail.domain.recommendation.controller.docs;

import com.example.moodtail.domain.recommendation.dto.response.PairRecommendationResponse;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Recommendations", description = "추천 결과, 맛 지표 매칭 API")
public interface RecommendationControllerDocs {

    @Operation(
            operationId = "recommendPair",
            summary = "페어 추천 결과 조회",
            description = "로그인한 사용자의 최신 감정 테스트 결과와, partnerInviteCode로 조회한 상대방의 최신 결과를 "
                    + "평균낸 타협 맛 지표를 기준으로 칵테일 3종을 추천합니다. 로그인이 필요합니다."
    )
    BaseResponse<PairRecommendationResponse> recommendPair(
            PrincipalDetails principalDetails,
            String partnerInviteCode
    );
}
