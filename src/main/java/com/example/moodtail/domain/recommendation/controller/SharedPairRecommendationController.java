package com.example.moodtail.domain.recommendation.controller;

import com.example.moodtail.domain.recommendation.dto.response.PairRecommendationShareResultResponse;
import com.example.moodtail.domain.recommendation.service.PairRecommendationShareQueryService;
import com.example.moodtail.global.common.base.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/share/pair-recommendations")
public class SharedPairRecommendationController {

    private final PairRecommendationShareQueryService pairRecommendationShareQueryService;

    @Operation(
            operationId = "getSharedPairRecommendation",
            summary = "공유된 페어 추천 결과 조회"
    )
    @GetMapping("/{token}")
    public BaseResponse<PairRecommendationShareResultResponse> getSharedPairRecommendation(
            @PathVariable String token
    ) {
        PairRecommendationShareResultResponse response = pairRecommendationShareQueryService.getSharedResult(token);
        return BaseResponse.onSuccess(response);
    }
}
