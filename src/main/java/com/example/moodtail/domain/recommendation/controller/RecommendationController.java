package com.example.moodtail.domain.recommendation.controller;


import com.example.moodtail.domain.recommendation.dto.request.PairRecommendationRequest;
import com.example.moodtail.domain.recommendation.dto.response.PairRecommendationResponse;
import com.example.moodtail.domain.recommendation.service.PairRecommendationService;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
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
            description = "로그인한 사용자의 최신 감정 테스트 결과와, partnerInviteCode로 조회한 상대방의 최신 결과를 "
                    + "평균낸 타협 맛 지표를 기준으로 칵테일 3종을 추천합니다. 로그인이 필요합니다."
    )
    @PostMapping("/recommends/pair")
    public BaseResponse<PairRecommendationResponse> recommendPair(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody PairRecommendationRequest request
            ) {
        PairRecommendationResponse response = pairRecommendationService.recommendPair(
                principalDetails.getUserId(),
                request.partnerInviteCode()
        );
        return BaseResponse.onSuccess(response);
    }

}
