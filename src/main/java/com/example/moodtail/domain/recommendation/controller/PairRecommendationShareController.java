package com.example.moodtail.domain.recommendation.controller;

import com.example.moodtail.domain.recommendation.controller.docs.PairRecommendationShareControllerDocs;
import com.example.moodtail.domain.recommendation.dto.request.PairRecommendationShareRequest;
import com.example.moodtail.domain.recommendation.dto.response.PairRecommendationShareResponse;
import com.example.moodtail.domain.recommendation.service.PairRecommendationShareService;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
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
public class PairRecommendationShareController implements PairRecommendationShareControllerDocs {

    private final PairRecommendationShareService pairRecommendationShareService;

    @Override
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
