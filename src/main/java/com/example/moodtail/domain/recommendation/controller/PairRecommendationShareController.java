package com.example.moodtail.domain.recommendation.controller;

import com.example.moodtail.domain.recommendation.dto.request.PairRecommendationShareRequest;
import com.example.moodtail.domain.recommendation.dto.response.PairRecommendationShareResponse;
import com.example.moodtail.domain.recommendation.service.PairRecommendationShareService;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cocktails/recommends/pair")
public class PairRecommendationShareController {

    private final PairRecommendationShareService pairRecommendationShareService;

    @Operation(
            operationId = "sharePairRecommendation",
            summary = "페어 추천 결과 SNS 공유 생성",
            description = "프론트가 이미 계산한 페어 추천 결과(타협 취향 프로필, 추천 칵테일 3개, 일치율)를 그대로 전달받아 "
                    + "SNS 공유용 스냅샷을 생성합니다. 서버는 재계산 없이 전달받은 값을 그대로 저장합니다."
    )
    @PostMapping("/share")
    public BaseResponse<PairRecommendationShareResponse> sharePairRecommendation(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody PairRecommendationShareRequest request
    ) {
        PairRecommendationShareResponse response = pairRecommendationShareService.createShare(
                principalDetails.getUserId(),
                request
        );
        return BaseResponse.onSuccess(response);
    }
}
