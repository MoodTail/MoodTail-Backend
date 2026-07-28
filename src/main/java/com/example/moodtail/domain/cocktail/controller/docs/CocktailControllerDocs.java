package com.example.moodtail.domain.cocktail.controller.docs;

import com.example.moodtail.domain.cocktail.dto.request.CustomCocktailRecommendationRequest;
import com.example.moodtail.domain.cocktail.dto.response.CocktailDetailResponse;
import com.example.moodtail.domain.cocktail.dto.response.CocktailFavoriteListResponse;
import com.example.moodtail.domain.cocktail.dto.response.CocktailFavoriteResponse;
import com.example.moodtail.domain.cocktail.dto.response.CocktailListResponse;
import com.example.moodtail.domain.cocktail.dto.response.CustomCocktailRecommendationResponse;
import com.example.moodtail.domain.cocktail.dto.response.DailyCocktailResponse;
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

import java.math.BigDecimal;

@Tag(
        name = "Cocktails",
        description = "칵테일 조회, 검색 및 추천 API"
)
public interface CocktailControllerDocs {
    String COCKTAIL_LIST_SUCCESS_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "COMMON200",
                "message": "요청에 성공했습니다.",
                "result": {
                  "cocktails": [
                    {
                      "cocktailId": 1,
                      "nameKo": "모히또",
                      "nameEn": "Mojito",
                      "alcoholDegree": 15.0,
                      "description": "라임과 민트의 청량함이 특징인 칵테일입니다.",
                      "imageUrl": "https://cdn.moodtail.com/images/mojito.png",
                      "isFavorite": true
                    },
                    {
                      "cocktailId": 2,
                      "nameKo": "피나 콜라다",
                      "nameEn": "Pina Colada",
                      "alcoholDegree": 13.0,
                      "description": "파인애플과 코코넛의 달콤한 칵테일입니다.",
                      "imageUrl": "https://cdn.moodtail.com/images/pina-colada.png",
                      "isFavorite": false
                    }
                  ]
                }
              }
              """;

    String CUSTOM_RECOMMENDATION_SUCCESS_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "COMMON200",
                "message": "요청에 성공했습니다.",
                "result": {
                  "cocktailId": 1,
                  "name": "모히또",
                  "description": "상큼하고 청량한 취향에 잘 어울리는 칵테일입니다.",
                  "imageUrl": "https://cdn.moodtail.com/images/mojito.png",
                  "matchRate": 92,
                  "userFigures": {
                    "alcoholIntensity": 40,
                    "sweetness": 60,
                    "sourness": 80,
                    "refreshing": 90,
                    "bitterness": 20
                  },
                  "cocktailFigures": {
                    "alcoholIntensity": 35,
                    "sweetness": 55,
                    "sourness": 75,
                    "refreshing": 95,
                    "bitterness": 15
                  }
                }
              }
              """;

    String DAILY_COCKTAIL_SUCCESS_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "COMMON200",
                "message": "요청에 성공했습니다.",
                "result": {
                  "recommendationSaved": true,
                  "context": {
                    "temperature": 28.5,
                    "humidity": 70,
                    "weather": "CLEAR",
                    "day": "수요일"
                  },
                  "cocktail": {
                    "cocktailId": 1,
                    "nameKo": "모히또",
                    "nameEn": "Mojito",
                    "shortDescription": "라임과 민트의 청량함이 특징인 칵테일입니다.",
                    "imageUrl": "https://cdn.moodtail.com/images/mojito.png",
                    "matchScore": 92
                  }
                }
              }
              """;

    String COCKTAIL400_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "COCKTAIL400",
                "message": "칵테일 도수 범위가 올바르지 않습니다."
              }
              """;

    String COMMON402_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "COMMON402",
                "message": "입력값 검증에 실패했습니다."
              }
              """;

    String COMMON406_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "COMMON406",
                "message": "요청 본문 형식이 올바르지 않습니다."
              }
              """;

    String COMMON401_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "COMMON401",
                "message": "인증이 필요합니다."
              }
              """;

    String AUTH006_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "AUTH006",
                "message": "유효하지 않은 액세스 토큰입니다."
              }
              """;

    String RECOMMENDATION422_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "RECOMMENDATION422",
                "message": "추천 결과를 산출할 수 없습니다."
              }
              """;

    String WEATHER500_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "WEATHER500",
                "message": "날씨 API 설정이 올바르지 않습니다."
              }
              """;

    String WEATHER502_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "WEATHER502",
                "message": "날씨 서비스의 응답을 처리할 수 없습니다."
              }
              """;

    String WEATHER503_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "WEATHER503",
                "message": "날씨 서비스를 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도해 주세요."
              }
              """;

    String WEATHER503_1_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "WEATHER503_1",
                "message": "날씨 API 호출 한도를 초과했습니다. 잠시 후 다시 시도해 주세요."
              }
              """;

    String COMMON500_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "COMMON500",
                "message": "서버 오류가 발생했습니다."
              }
              """;

    String COCKTAIL_DETAIL_SUCCESS_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "COMMON200",
                "message": "요청에 성공했습니다.",
                "result": {
                  "cocktailId": 15,
                  "name": "모히또",
                  "shortDescription": "가볍고 청량한 럼 베이스 칵테일",
                  "imageUrl": "https://cdn.moodtail.com/images/mojito.png",
                  "alcoholIntensity": 2.0,
                  "sweetness": 3.0,
                  "sourness": 4.0,
                  "refreshing": 5.0,
                  "bitterness": 1.0,
                  "isFavorite": false,
                  "recipeSteps": [
                    {
                      "stepOrder": 1,
                      "description": "라임과 설탕을 넣고 으깬다"
                    }
                  ],
                  "cocktailIngredients": [
                    {
                      "name": "화이트 럼",
                      "amountText": "50ML",
                      "sortOrder": 1
                    }
                  ]
                }
              }
              """;

    String COCKTAIL404_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "COCKTAIL404",
                "message": "해당 칵테일을 찾을 수 없습니다."
              }
              """;

    String RECIPE404_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "RECIPE404",
                "message": "해당 레시피를 찾을 수 없습니다."
              }
              """;

    String INGREDIENT404_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "INGREDIENT404",
                "message": "해당 재료를 찾을 수 없습니다."
              }
              """;

    @Operation(
            operationId = "getCocktailDetail",
            summary = "칵테일 정보/레시피 상세조회",
            description = "칵테일 정보, 레시피, 재료, 즐겨찾기 여부를 반환합니다. 인증이 필요하지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "COMMON200 - 칵테일 상세 조회 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = COCKTAIL_DETAIL_SUCCESS_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "COCKTAIL404/RECIPE404/INGREDIENT404 - 칵테일, 레시피 또는 재료를 찾을 수 없음",
                    content = @Content(examples = {
                            @ExampleObject(name = "COCKTAIL404", value = COCKTAIL404_EXAMPLE),
                            @ExampleObject(name = "RECIPE404", value = RECIPE404_EXAMPLE),
                            @ExampleObject(name = "INGREDIENT404", value = INGREDIENT404_EXAMPLE)
                    })
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE))
            )
    })
    BaseResponse<CocktailDetailResponse> getCocktailDetail(
            @Parameter(description = "칵테일 ID", example = "15")
            Long cocktailId,

            @Parameter(hidden = true)
            PrincipalDetails principalDetails
    );

    String FAVORITE_LIST_SUCCESS_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "COMMON200",
                "message": "요청에 성공했습니다.",
                "result": {
                  "cocktails": [
                    {
                      "cocktailId": 15,
                      "name": "모히또",
                      "description": "가볍고 청량한 럼 베이스 칵테일",
                      "imageUrl": "https://cdn.moodtail.com/images/mojito.png",
                      "isFavorite": true
                    }
                  ]
                }
              }
              """;

    String FAVORITE_ADD_SUCCESS_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "COMMON200",
                "message": "요청에 성공했습니다.",
                "result": {
                  "cocktailId": 15,
                  "name": "모히또"
                }
              }
              """;

    String FAVORITE_REMOVE_SUCCESS_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "COMMON200",
                "message": "요청에 성공했습니다.",
                "result": {
                  "cocktailId": 15,
                  "name": "모히또"
                }
              }
              """;

    String COCKTAIL409_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "COCKTAIL409",
                "message": "이미 즐겨찾기에 추가된 칵테일입니다."
              }
              """;

    String FAVORITE404_EXAMPLE = """
              {
                "timestamp": "2026-07-22T14:30:00",
                "code": "FAVORITE404",
                "message": "즐겨찾기에 추가되지 않은 칵테일입니다."
              }
              """;

    @Operation(
            operationId = "getFavoriteCocktails",
            summary = "칵테일 즐겨찾기 목록 조회",
            description = "인증된 사용자가 즐겨찾기한 칵테일 목록을 반환합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "COMMON200 - 즐겨찾기 목록 조회 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = FAVORITE_LIST_SUCCESS_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = """
                              COMMON401 - 인증 정보가 없음
                              AUTH006 - 액세스 토큰이 만료됐거나 유효하지 않음
                              """,
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON401", value = COMMON401_EXAMPLE),
                            @ExampleObject(name = "AUTH006", value = AUTH006_EXAMPLE)
                    })
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE))
            )
    })
    BaseResponse<CocktailFavoriteListResponse> getFavoriteCocktails(
            @Parameter(hidden = true)
            PrincipalDetails principalDetails
    );

    @Operation(
            operationId = "addFavorite",
            summary = "칵테일 즐겨찾기 추가",
            description = "인증된 사용자가 칵테일을 즐겨찾기에 추가합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "COMMON200 - 즐겨찾기 추가 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = FAVORITE_ADD_SUCCESS_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = """
                              COMMON401 - 인증 정보가 없음
                              AUTH006 - 액세스 토큰이 만료됐거나 유효하지 않음
                              """,
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON401", value = COMMON401_EXAMPLE),
                            @ExampleObject(name = "AUTH006", value = AUTH006_EXAMPLE)
                    })
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "COCKTAIL404 - 칵테일을 찾을 수 없음",
                    content = @Content(examples = @ExampleObject(name = "COCKTAIL404", value = COCKTAIL404_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "COCKTAIL409 - 이미 즐겨찾기에 추가된 칵테일",
                    content = @Content(examples = @ExampleObject(name = "COCKTAIL409", value = COCKTAIL409_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE))
            )
    })
    BaseResponse<CocktailFavoriteResponse> addFavorite(
            @Parameter(description = "칵테일 ID", example = "15")
            Long cocktailId,

            @Parameter(hidden = true)
            PrincipalDetails principalDetails
    );

    @Operation(
            operationId = "removeFavorite",
            summary = "칵테일 즐겨찾기 삭제",
            description = "인증된 사용자가 칵테일을 즐겨찾기에서 삭제합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "COMMON200 - 즐겨찾기 삭제 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = FAVORITE_REMOVE_SUCCESS_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = """
                              COMMON401 - 인증 정보가 없음
                              AUTH006 - 액세스 토큰이 만료됐거나 유효하지 않음
                              """,
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON401", value = COMMON401_EXAMPLE),
                            @ExampleObject(name = "AUTH006", value = AUTH006_EXAMPLE)
                    })
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "COCKTAIL404/FAVORITE404 - 칵테일 또는 즐겨찾기를 찾을 수 없음",
                    content = @Content(examples = {
                            @ExampleObject(name = "COCKTAIL404", value = COCKTAIL404_EXAMPLE),
                            @ExampleObject(name = "FAVORITE404", value = FAVORITE404_EXAMPLE)
                    })
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE))
            )
    })
    BaseResponse<CocktailFavoriteResponse> removeFavorite(
            @Parameter(description = "칵테일 ID", example = "15")
            Long cocktailId,

            @Parameter(hidden = true)
            PrincipalDetails principalDetails
    );

    @Operation(
            operationId = "getCocktails",
            summary = "칵테일 도수 및 이름 검색",
            description = """
                      최소 도수, 최대 도수 또는 한글·영문 이름으로 칵테일을 검색합니다.
                      검색 결과가 없으면 빈 목록을 반환합니다.
                      """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "COMMON200 - 칵테일 목록 조회 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = COCKTAIL_LIST_SUCCESS_EXAMPLE))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                              COCKTAIL400 - 음수 도수 또는 최소 도수가 최대 도수보다 큼
                              """,
                    content = @Content(examples = {
                            @ExampleObject(name = "COCKTAIL400", value = COCKTAIL400_EXAMPLE)
                    })
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "COMMON500 - 서버 내부 오류",
                    content = @Content(
                            examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)
                    )
            )
    })
    BaseResponse<CocktailListResponse> getCocktails(
            @Parameter(
                    description = "최소 알코올 도수",
                    example = "5.0"
            )
            BigDecimal minAlcoholDegree,

            @Parameter(
                    description = "최대 알코올 도수",
                    example = "20.0"
            )
            BigDecimal maxAlcoholDegree,

            @Parameter(
                    description = "칵테일 한글 또는 영문 이름 검색어",
                    example = "모히또 or Mojito"
            )
            String keyword,

            @Parameter(hidden = true)
            PrincipalDetails principalDetails
    );


    @Operation(
            operationId = "recommendCustomCocktail",
            summary = "커스텀 칵테일 추천",
            description = """
                      사용자가 입력한 다섯 가지 맛 지표와 가장 가까운 칵테일 하나를 추천합니다.
                      각 맛 지표는 0 이상 100 이하의 정수이며 모든 항목은 필수입니다.
                      """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "COMMON200 - 커스텀 칵테일 추천 성공",
                    useReturnTypeSchema = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "COMMON200",
                                    value = CUSTOM_RECOMMENDATION_SUCCESS_EXAMPLE
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                              COMMON402 - 맛 지표 누락, null 또는 0~100 범위 위반
                              COMMON406 - JSON 형식 또는 필드 타입 오류
                              """,
                    content = @Content(examples = {
                            @ExampleObject(
                                    name = "COMMON402",
                                    value = COMMON402_EXAMPLE
                            ),
                            @ExampleObject(
                                    name = "COMMON406",
                                    value = COMMON406_EXAMPLE
                            )
                    })
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = """
                              COMMON401 - 인증 정보가 없음
                              AUTH006 - 액세스 토큰이 만료됐거나 유효하지 않음
                              """,
                    content = @Content(examples = {
                            @ExampleObject(
                                    name = "COMMON401",
                                    value = COMMON401_EXAMPLE
                            ),
                            @ExampleObject(
                                    name = "AUTH006",
                                    value = AUTH006_EXAMPLE
                            )
                    })
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "RECOMMENDATION422 - 추천할 칵테일 데이터가 없음",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "RECOMMENDATION422",
                                    value = RECOMMENDATION422_EXAMPLE
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "COMMON500 - 서버 내부 오류",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "COMMON500",
                                    value = COMMON500_EXAMPLE
                            )
                    )
            )
    })
    BaseResponse<CustomCocktailRecommendationResponse>
    recommendCustomCocktail(
            CustomCocktailRecommendationRequest request
    );

    @Operation(
            operationId = "getDailyCocktail",
            summary = "오늘의 칵테일 조회",
            description = """
                      서울의 현재 날씨와 요일을 기준으로 오늘의 칵테일을 추천합니다.
                      오늘 추천이 이미 생성된 경우 외부 날씨 API를 다시 호출하지 않고 저장된 추천을 반환합니다.
                      로그인하지 않아도 사용할 수 있습니다.
                      """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "COMMON200 - 오늘의 칵테일 조회 성공",
                    useReturnTypeSchema = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "COMMON200",
                                    value = DAILY_COCKTAIL_SUCCESS_EXAMPLE
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "RECOMMENDATION422 - 오늘의 칵테일 산출 불가",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "RECOMMENDATION422",
                                    value = RECOMMENDATION422_EXAMPLE
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = """
                              WEATHER500 - 날씨 API 설정 오류
                              COMMON500 - 서버 내부 오류
                              """,
                    content = @Content(examples = {
                            @ExampleObject(
                                    name = "WEATHER500",
                                    value = WEATHER500_EXAMPLE
                            ),
                            @ExampleObject(
                                    name = "COMMON500",
                                    value = COMMON500_EXAMPLE
                            )
                    })
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "WEATHER502 - 날씨 API 응답 처리 불가",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "WEATHER502",
                                    value = WEATHER502_EXAMPLE
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = """
                              WEATHER503 - 날씨 서비스 이용 불가
                              WEATHER503_1 - 날씨 API 호출 한도 초과
                              """,
                    content = @Content(examples = {
                            @ExampleObject(
                                    name = "WEATHER503",
                                    value = WEATHER503_EXAMPLE
                            ),
                            @ExampleObject(
                                    name = "WEATHER503_1",
                                    value = WEATHER503_1_EXAMPLE
                            )
                    })
            )
    })
    BaseResponse<DailyCocktailResponse> getDailyCocktail();
}
