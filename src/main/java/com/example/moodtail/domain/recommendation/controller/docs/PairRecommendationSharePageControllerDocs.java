package com.example.moodtail.domain.recommendation.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Pair Recommendation Shares", description = "페어 추천 결과 공유 URL 생성 및 공개 결과 조회 API")
public interface PairRecommendationSharePageControllerDocs {

    @Operation(operationId = "getPairRecommendationSharePage", summary = "공유된 페어 추천 결과 OG 메타데이터 페이지 조회",
            description = "SNS 크롤러에는 저장된 썸네일의 OG 메타데이터를 제공하고 일반 브라우저는 프론트엔드 결과 페이지로 이동시킵니다.")
    ResponseEntity<String> getSharePage(
            @Parameter(description = "공유 결과 토큰", example = "p_hJ7JngQmYV4x0aP9k2LmN3Qr") String shareToken
    );
}
