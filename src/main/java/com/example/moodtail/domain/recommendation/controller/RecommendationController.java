package com.example.moodtail.domain.recommendation.controller;


import com.example.moodtail.domain.recommendation.dto.response.PairRecommendationResponse;
import com.example.moodtail.domain.recommendation.service.PairRecommendationService;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cocktails")
public class RecommendationController {
    private final PairRecommendationService pairRecommendationService;

    @Operation(
            operationId = "recommendPair",
            summary = "페어 추천 결과 조회",
            description = "두 사용자의 감정 테스트 결과를 평균낸 타협 맛 지표를 기준으로 칵테일 4종을 추천합니다. "
                    + "resultId 또는 resultShareToken 중 하나, partnerResultId 또는 partnerShareToken 중 하나가 필요합니다."
    )
    @GetMapping("/recommends/pair")
    public BaseResponse<PairRecommendationResponse> recommendPair(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam(required = false) Long resultId,
            @RequestParam(required = false) String resultShareToken,
            @RequestParam(required = false) Long partnerResultId,
            @RequestParam(required = false) String partnerShareToken
            ) {
        PairRecommendationResponse response = pairRecommendationService.recommendPair(
                principalDetails.getUserId(),
                resultId,
                resultShareToken,
                partnerResultId,
                partnerShareToken
        );
        return BaseResponse.onSuccess(response);
    }

}
