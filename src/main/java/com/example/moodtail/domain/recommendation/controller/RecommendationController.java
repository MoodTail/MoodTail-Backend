package com.example.moodtail.domain.recommendation.controller;


import com.example.moodtail.domain.recommendation.controller.docs.RecommendationControllerDocs;
import com.example.moodtail.domain.recommendation.dto.response.PairRecommendationResponse;
import com.example.moodtail.domain.recommendation.service.PairRecommendationService;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cocktails")
public class RecommendationController implements RecommendationControllerDocs {
    private final PairRecommendationService pairRecommendationService;

    @GetMapping("/recommends/pair")
    public BaseResponse<PairRecommendationResponse> recommendPair(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam String partnerInviteCode
            ) {
        PairRecommendationResponse response = pairRecommendationService.recommendPair(
                principalDetails.getUserId(),
                partnerInviteCode
        );
        return BaseResponse.onSuccess(response);
    }

}
