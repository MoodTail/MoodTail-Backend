package com.example.moodtail.domain.cocktail.controller.docs;

import com.example.moodtail.domain.cocktail.dto.response.CocktailTrendResponse;
import com.example.moodtail.global.common.base.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Cocktails", description = "칵테일 목록, 상세, 레시피, 즐겨찾기 API")
public interface CocktailTrendControllerDocs {

    String TREND_SUCCESS_EXAMPLE = """
            {"timestamp":"2026-07-22T10:00:00","code":"COMMON200","message":"요청에 성공했습니다.","result":{
              "period":"WEEKLY",
              "popularMoodTypes":[
                {"ranking":1,"moodTypeId":3,"typeCode":"FRESH_SPARK","name":"상큼주의자","resultCount":128,"ratio":34,"rankChange":2}
              ],
              "averageTasteProfile":{"alcoholIntensity":2.25,"sweetness":3.10,"sourness":2.90,"refreshing":3.70,"bitterness":1.55},
              "displayAverageTasteScores":{"alcoholIntensity":45,"sweetness":62,"sourness":58,"refreshing":74,"bitterness":31},
              "popularCocktails":[
                {"ranking":1,"cocktailId":7,"nameKo":"피치 하이볼","nameEn":"Peach Highball","shortDescription":"달콤하고 청량한 추천","ratio":21,"recordCount":34,"rankChange":1},
                {"ranking":2,"cocktailId":8,"nameKo":"선라이즈 소다","nameEn":"Sunrise Soda","shortDescription":"과일향 중심의 추천","ratio":18,"recordCount":29,"rankChange":0}
              ],
              "rankChangeCocktails":[
                {"cocktailId":9,"nameKo":"모히토","nameEn":"Mojito","rankChange":6,"changeDirection":"UP"},
                {"cocktailId":15,"nameKo":"진 토닉","nameEn":"Gin Tonic","rankChange":4,"changeDirection":"DOWN"}
              ],
              "sameTypePopularCocktails":[
                {"ranking":1,"cocktailId":12,"nameKo":"선샤인 피즈","recordCount":12}
              ]
            }}
            """;
    String TREND_400_EXAMPLE = """
            {"timestamp":"2026-07-22T10:00:00","code":"TREND_400","message":"period 값이 올바르지 않습니다. DAILY, WEEKLY, MONTHLY 중 하나여야 합니다."}
            """;
    String COMMON500_EXAMPLE = """
            {"timestamp":"2026-07-22T10:00:00","code":"COMMON500","message":"서버 에러가 발생했습니다."}
            """;

    @Operation(operationId = "getCocktailTrend", summary = "칵테일 트렌드 집계 조회",
            description = "테스트 기록과 추천 결과를 기반으로 기간별 인기 타입, 평균 취향, 인기 칵테일과 순위 변동을 반환합니다. 인증이 필요하지 않습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 칵테일 트렌드 조회 성공", useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = TREND_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "TREND_400 - period 값이 DAILY/WEEKLY/MONTHLY 중 하나가 아님",
                    content = @Content(examples = @ExampleObject(name = "TREND_400", value = TREND_400_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    BaseResponse<CocktailTrendResponse> getCocktailTrend(
            @Parameter(description = "집계 기간. DAILY, WEEKLY, MONTHLY 중 하나. 기본값 WEEKLY", example = "WEEKLY") String period,
            @Parameter(description = "특정 무드타입 기준 칵테일 집계. 전달 시 응답에 sameTypePopularCocktails가 포함됩니다.", example = "3") Long moodTypeId
    );
}
