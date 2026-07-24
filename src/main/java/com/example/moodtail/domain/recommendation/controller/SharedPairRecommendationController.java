package com.example.moodtail.domain.recommendation.controller;

import com.example.moodtail.domain.recommendation.controller.docs.SharedPairRecommendationControllerDocs;
import com.example.moodtail.domain.recommendation.dto.response.PairRecommendationShareResultResponse;
import com.example.moodtail.domain.recommendation.service.PairRecommendationShareQueryService;
import com.example.moodtail.global.common.base.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/share/pair-recommendations")
public class SharedPairRecommendationController implements SharedPairRecommendationControllerDocs {

    private final PairRecommendationShareQueryService pairRecommendationShareQueryService;

    @GetMapping("/{shareToken}")
    public BaseResponse<PairRecommendationShareResultResponse> getSharedPairRecommendation(
            @PathVariable String shareToken
    ) {
        PairRecommendationShareResultResponse response = pairRecommendationShareQueryService.getSharedResult(shareToken);
        return BaseResponse.onSuccess(response);
    }
}
