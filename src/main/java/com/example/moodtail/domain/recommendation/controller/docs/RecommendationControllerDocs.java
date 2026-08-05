package com.example.moodtail.domain.recommendation.controller.docs;

import com.example.moodtail.domain.recommendation.dto.response.PairRecommendationResponse;
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

@Tag(name = "Recommendations", description = "추천 결과, 맛 지표 매칭 API")
public interface RecommendationControllerDocs {

    String RECOMMEND_PAIR_SUCCESS_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "COMMON200",
                "message": "요청에 성공했습니다.",
                "result": {
                  "myNickname": "사용자",
                  "partnerNickname": "파트너",
                  "myProfile": {
                    "alcoholIntensity": 3.0,
                    "sweetness": 4.5,
                    "sourness": 2.0,
                    "refreshing": 4.0,
                    "bitterness": 1.5
                  },
                  "partnerProfile": {
                    "alcoholIntensity": 4.0,
                    "sweetness": 3.0,
                    "sourness": 2.5,
                    "refreshing": 3.5,
                    "bitterness": 2.5
                  },
                  "compromiseProfile": {
                    "alcoholIntensity": 3.5,
                    "sweetness": 3.8,
                    "sourness": 2.2,
                    "refreshing": 3.8,
                    "bitterness": 2.0
                  },
                  "recommendations": [
                    {
                      "ranking": 1,
                      "cocktailId": 18,
                      "nameKo": "프렌치 75",
                      "nameEn": "French 75",
                      "matchScore": 93,
                      "myMatchScore": 90,
                      "partnerMatchScore": 85
                    }
                  ],
                  "tasteContributions": [
                    {
                      "metric": "sweetness",
                      "metricNameKo": "달콤함",
                      "dominantSide": "ME"
                    }
                  ]
                }
              }
              """;

    String MOOD_TEST_404_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "MOOD_TEST_404",
                "message": "테스트 결과를 찾을 수 없습니다."
              }
              """;

    String INVITE_CODE404_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "INVITE_CODE404",
                "message": "초대 코드에 해당하는 사용자를 찾을 수 없습니다."
              }
              """;

    String RECOMMENDATION422_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "RECOMMENDATION422",
                "message": "추천 결과를 산출할 수 없습니다."
              }
              """;

    String INVITE_CODE400_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "INVITE_CODE400",
                "message": "초대 코드 형식이 올바르지 않습니다."
              }
              """;

    String RECOMMENDATION400_SELF_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "RECOMMENDATION400",
                "message": "본인의 초대 코드로는 페어 추천을 받을 수 없습니다."
              }
              """;

    @Operation(
            operationId = "recommendPair",
            summary = "같이 고르기 결과 조회",
            description = "로그인한 사용자의 최신 감정 테스트 결과와, partnerInviteCode로 조회한 상대방의 최신 결과를 "
                    + "평균낸 타협 맛 지표를 기준으로 칵테일 3종을 추천합니다. 로그인이 필요합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "COMMON200 - 같이 고르기 결과 조회 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = RECOMMEND_PAIR_SUCCESS_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "INVITE_CODE400/RECOMMENDATION400 - 초대 코드 형식 오류 또는 본인 초대 코드 입력",
                    content = @Content(examples = {
                            @ExampleObject(name = "INVITE_CODE400", value = INVITE_CODE400_EXAMPLE),
                            @ExampleObject(name = "RECOMMENDATION400", value = RECOMMENDATION400_SELF_EXAMPLE)
                    })
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "MOOD_TEST_404/INVITE_CODE404 - 테스트 결과 또는 초대 코드에 해당하는 사용자를 찾을 수 없음",
                    content = @Content(examples = {
                            @ExampleObject(name = "MOOD_TEST_404", value = MOOD_TEST_404_EXAMPLE),
                            @ExampleObject(name = "INVITE_CODE404", value = INVITE_CODE404_EXAMPLE)
                    })
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "RECOMMENDATION422 - 추천 결과를 산출할 수 없음",
                    content = @Content(examples = @ExampleObject(name = "RECOMMENDATION422", value = RECOMMENDATION422_EXAMPLE))
            )
    })
    BaseResponse<PairRecommendationResponse> recommendPair(
            @Parameter(hidden = true)
            PrincipalDetails principalDetails,

            @Parameter(description = "상대방 초대 코드", example = "MOOD-4821")
            String partnerInviteCode
    );
}
