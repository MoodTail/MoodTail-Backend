package com.example.moodtail.domain.recommendation.controller;

import com.example.moodtail.domain.recommendation.controller.docs.PairRecommendationShareImageControllerDocs;
import com.example.moodtail.domain.recommendation.dto.response.PairRecommendationShareImageResponse;
import com.example.moodtail.domain.recommendation.service.PairRecommendationShareImageService;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cocktails/recommends/pair")
public class PairRecommendationShareImageController implements PairRecommendationShareImageControllerDocs {

    private final PairRecommendationShareImageService pairRecommendationShareImageService;

    @PostMapping(
            path = "/share-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public BaseResponse<PairRecommendationShareImageResponse> uploadPairRecommendationShareImage(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam String partnerInviteCode,
            @RequestPart("image") MultipartFile image
    ) {
        PairRecommendationShareImageResponse response = pairRecommendationShareImageService.uploadShareImage(
                principalDetails.getUserId(),
                partnerInviteCode,
                image
        );
        return BaseResponse.onSuccess(response);
    }
}
