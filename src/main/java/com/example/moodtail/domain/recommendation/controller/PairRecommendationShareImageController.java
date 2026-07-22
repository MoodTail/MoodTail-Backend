package com.example.moodtail.domain.recommendation.controller;

import com.example.moodtail.domain.recommendation.dto.response.PairRecommendationShareImageResponse;
import com.example.moodtail.domain.recommendation.service.PairRecommendationShareImageService;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
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
public class PairRecommendationShareImageController {

    private final PairRecommendationShareImageService pairRecommendationShareImageService;

    @Operation(
            operationId = "uploadPairRecommendationShareImage",
            summary = "페어 추천 결과 공유 이미지 업로드",
            description = "partnerInviteCode로 상대방을 다시 조회해 페어 추천 결과(초대 코드 유효성, 양쪽 최신 감정 테스트 결과 존재 여부)를 "
                    + "재검증한 뒤, 전달받은 이미지를 S3에 저장하고 공유 이미지 URL을 반환합니다. 로그인이 필요합니다."
    )
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
