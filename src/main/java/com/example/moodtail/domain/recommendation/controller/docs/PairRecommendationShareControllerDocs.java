package com.example.moodtail.domain.recommendation.controller.docs;

import com.example.moodtail.domain.recommendation.dto.request.PairRecommendationShareRequest;
import com.example.moodtail.domain.recommendation.dto.response.PairRecommendationShareResponse;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Pair Recommendation Shares", description = "페어 추천 결과 공유 URL 생성 및 공개 결과 조회 API")
public interface PairRecommendationShareControllerDocs {

    String SHARE_PAIR_SUCCESS_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "COMMON200",
                "message": "요청에 성공했습니다.",
                "result": {
                  "shareToken": "p_9f3ab21",
                  "shareUrl": "https://moodtail.com/share/pair/p_9f3ab21"
                }
              }
              """;

    String COMMON402_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "COMMON402",
                "message": "입력값 검증에 실패했습니다."
              }
              """;

    String SHARE_400_SIZE_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "SHARE_400",
                "message": "추천 칵테일은 정확히 3개여야 합니다."
              }
              """;

    String SHARE_400_COCKTAIL_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "SHARE_400",
                "message": "존재하지 않는 칵테일이 포함되어 있습니다."
              }
              """;

    String AUTH010_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "AUTH010",
                "message": "존재하지 않는 사용자입니다."
              }
              """;

    String COMMON500_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "COMMON500",
                "message": "서버 에러가 발생했습니다."
              }
              """;

    @Operation(
            operationId = "sharePairRecommendation",
            summary = "페어 추천 결과 SNS 공유 생성",
            description = "프론트가 이미 계산한 페어 추천 결과(타협 취향 프로필, 추천 칵테일 3개, 일치율)를 그대로 전달받아 "
                    + "SNS 공유용 스냅샷을 생성합니다. 서버는 재계산 없이 전달받은 값을 그대로 저장합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "COMMON200 - 페어 추천 결과 공유 생성 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = SHARE_PAIR_SUCCESS_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                              COMMON402 - 요청 값 검증 실패
                              SHARE_400 - 추천 칵테일 개수 불일치 또는 존재하지 않는 칵테일 포함
                              """,
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "SHARE_400 (개수 불일치)", value = SHARE_400_SIZE_EXAMPLE),
                            @ExampleObject(name = "SHARE_400 (칵테일 없음)", value = SHARE_400_COCKTAIL_EXAMPLE)
                    })
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "AUTH010 - 인증된 사용자가 존재하지 않음",
                    content = @Content(examples = @ExampleObject(name = "AUTH010", value = AUTH010_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE))
            )
    })
    BaseResponse<PairRecommendationShareResponse> sharePairRecommendation(
            @Parameter(hidden = true)
            PrincipalDetails principalDetails,

            PairRecommendationShareRequest request
    );
}
