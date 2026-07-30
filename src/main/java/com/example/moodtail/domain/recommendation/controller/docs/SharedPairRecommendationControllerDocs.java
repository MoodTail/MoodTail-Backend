package com.example.moodtail.domain.recommendation.controller.docs;

import com.example.moodtail.domain.recommendation.dto.response.PairRecommendationShareResultResponse;
import com.example.moodtail.global.common.base.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Pair Recommendation Shares", description = "페어 추천 결과 공유 URL 생성 및 공개 결과 조회 API")
public interface SharedPairRecommendationControllerDocs {

    String GET_SHARED_PAIR_RECOMMENDATION_SUCCESS_EXAMPLE = """
            {"timestamp":"2026-07-29T14:30:00","code":"COMMON200","message":"요청에 성공했습니다.","result":{"compromiseProfile":{"alcoholIntensity":3.5,"sweetness":3.8,"sourness":3.2,"refreshing":4.0,"bitterness":2.1},"recommendations":[{"ranking":1,"cocktailId":18,"nameKo":"프렌치 75","nameEn":"French 75","matchScore":93}],"myMatchScore":70,"partnerMatchScore":85,"thumbnailImageUrl":"https://cdn.moodtail.com/share/thumb_abc123.png"}}
            """;
    String SHARE404_EXAMPLE = """
            {"timestamp":"2026-07-29T14:30:00","code":"SHARE404","message":"존재하지 않는 공유 토큰입니다."}
            """;
    String COMMON500_EXAMPLE = """
            {"timestamp":"2026-07-29T14:30:00","code":"COMMON500","message":"서버 에러가 발생했습니다."}
            """;

    @Operation(
            operationId = "getSharedPairRecommendation",
            summary = "공유된 페어 추천 결과 조회"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 공유된 페어 추천 결과 조회 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(
                            name = "COMMON200",
                            value = GET_SHARED_PAIR_RECOMMENDATION_SUCCESS_EXAMPLE
                    ))),
            @ApiResponse(responseCode = "404", description = "SHARE404 - 존재하지 않는 공유 토큰",
                    content = @Content(examples = @ExampleObject(name = "SHARE404", value = SHARE404_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    BaseResponse<PairRecommendationShareResultResponse> getSharedPairRecommendation(
            @Parameter(description = "페어 추천 결과 공유 토큰", example = "9f3ab21c-1234-4a56-9abc-7890def12345")
            String shareToken
    );
}
