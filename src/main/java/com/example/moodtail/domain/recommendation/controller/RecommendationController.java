package com.example.moodtail.domain.recommendation.controller;


import com.example.moodtail.domain.recommendation.dto.request.PairRecommendationRequest;
import com.example.moodtail.domain.recommendation.dto.response.PairRecommendationResponse;
import com.example.moodtail.domain.recommendation.service.PairRecommendationService;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cocktails")
public class RecommendationController {
    private final PairRecommendationService pairRecommendationService;

    @Operation(
            operationId = "recommendPair",
            summary = "페어 추천 결과 조회",
            description = "두 사용자의 감정 테스트 결과를 평균낸 타협 맛 지표를 기준으로 칵테일 4종을 추천합니다. "
                    + "resultId 또는 resultShareToken 중 하나, partnerShareToken이 필요합니다."
    )
    @PostMapping("/recommends/pair")
    public BaseResponse<PairRecommendationResponse> recommendPair(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody PairRecommendationRequest request
            ) {
        PairRecommendationResponse response = pairRecommendationService.recommendPair(
                principalDetails != null ? principalDetails.getUserId() : null,
                request.resultId(),
                request.resultShareToken(),
                request.partnerShareToken()
        );
        return BaseResponse.onSuccess(response);
    }

}
