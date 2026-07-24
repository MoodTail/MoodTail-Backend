package com.example.moodtail.domain.recommendation.controller.docs;

import com.example.moodtail.domain.recommendation.dto.response.PairRecommendationShareImageResponse;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Pair Recommendation Shares", description = "페어 추천 결과 공유 URL 생성 및 공개 결과 조회 API")
public interface PairRecommendationShareImageControllerDocs {

    @Operation(
            operationId = "uploadPairRecommendationShareImage",
            summary = "페어 추천 결과 공유 이미지 업로드",
            description = "partnerInviteCode로 상대방을 다시 조회해 페어 추천 결과(초대 코드 유효성, 양쪽 최신 감정 테스트 결과 존재 여부)를 "
                    + "재검증한 뒤, 전달받은 이미지를 S3에 저장하고 공유 이미지 URL을 반환합니다. 로그인이 필요합니다."
    )
    BaseResponse<PairRecommendationShareImageResponse> uploadPairRecommendationShareImage(
            PrincipalDetails principalDetails,
            String partnerInviteCode,
            MultipartFile image
    );
}
