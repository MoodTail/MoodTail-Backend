package com.example.moodtail.domain.cocktail.controller.docs;

import com.example.moodtail.domain.cocktail.dto.response.MoodTypeResponse;
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

@Tag(
        name = "Mood Types",
        description = "도감 무드 타입 상세 조회 API"
)
public interface MoodTypeControllerDocs {
    String MOOD_TYPE_SUCCESS_EXAMPLE = """
              {
                "timestamp": "2026-08-13T02:14:55.814912595",
                "code": "COMMON200",
                "message": "요청에 성공했습니다.",
                "result": {
                  "moodTypeId": 2,
                  "typeCode": "passionate-challenger",
                  "name": "열정적인 도전자",
                  "shortDescription": "달콤하고 상큼한 것에 끌리는, 언제나 먼저 달려가는 타입",
                  "description": "달콤하고 상큼한 맛에 거침없이 손을 뻗는 타입이에요. 새로운 칵테일 앞에서도 망설임 없이 \\"이거 마셔볼게요\\"를 외치죠. 오늘도 전속력으로 달리는 중이지만, 표정은 언제나 해맑아요. 에너지가 넘쳐서 가끔 주변 사람들이 지칠 때도 있지만 그 열기에 결국 모두가 같이 달리게 되는 타입이에요.",
                  "catchphrase": "망설일 시간에 한 잔 더!",
                  "characterImageUrl": "https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/mood-types/passionate-challenger.png",
                  "unlocked": true,
                  "representative": false,
                  "canSetRepresentative": true,
                  "typePercent": 33,
                  "collectionRate": 0,
                  "typeFigures": {
                    "alcoholIntensity": 42,
                    "sweetness": 60,
                    "sourness": 56,
                    "bitterness": 28,
                    "refreshing": 47
                  },
                  "compatibilities": {
                    "best": {
                      "moodTypeId": 8,
                      "typeCode": "refreshing-explorer",
                      "name": "청량한 탐험가",
                      "characterImageUrl": "https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/mood-types/refreshing-explorer.png"
                    },
                    "worst": {
                      "moodTypeId": 6,
                      "typeCode": "explosive-adventurer",
                      "name": "폭발적인 모험가",
                      "characterImageUrl": "https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/mood-types/explosive-adventurer.png"
                    }
                  },
                  "cocktails": [
                    {
                      "cocktailId": 9,
                      "nameKo": "코프스 리바이버 #2",
                      "nameEn": "Corpse Reviver #2",
                      "shortDescription": "묵직한 단맛 베이스에 톡 쏘는 신맛과 짙은 여운이 어우러진 목 넘김이 편한 칵테일",
                      "imageUrl": "https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/CORPSE%20REVIVER%20%232.png",
                      "unlocked": false
                    },
                    {
                      "cocktailId": 10,
                      "nameKo": "롱 아일랜드 아이스 티",
                      "nameEn": "Long Island Iced Tea",
                      "shortDescription": "부드럽고 매끄러운 진 베이스로, 녹진한 단맛과 짜릿한 산미가 조화를 이룬 깊은 쌉쌀함 칵테일",
                      "imageUrl": "https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/LONG%20ISLAND%20ICED%20TEA.png",
                      "unlocked": false
                    },
                    {
                      "cocktailId": 11,
                      "nameKo": "미모사",
                      "nameEn": "Mimosa",
                      "shortDescription": "강렬한 새콤함 산뜻함에 진한 달콤함을 더한 산뜻하고 가벼운 묵직한 쓴맛 칵테일",
                      "imageUrl": "https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/MIMOSA.png",
                      "unlocked": false
                    },
                    {
                      "cocktailId": 12,
                      "nameKo": "미셔너리스 다운폴",
                      "nameEn": "Missionary’s Downfall",
                      "shortDescription": "묵직한 단맛 베이스에 톡 쏘는 신맛과 깨끗한 끝맛이 어우러진 목 넘김이 편한 칵테일",
                      "imageUrl": "https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/MISSIONARY%27S%20DOWNFALL.png",
                      "unlocked": false
                    },
                    {
                      "cocktailId": 13,
                      "nameKo": "올드 쿠반",
                      "nameEn": "Old Cuban",
                      "shortDescription": "부드럽고 매끄러운 럼 베이스로, 녹진한 단맛과 짜릿한 산미가 조화를 이룬 칵테일",
                      "imageUrl": "https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/OLD%20CUBAN.png",
                      "unlocked": false
                    },
                    {
                      "cocktailId": 14,
                      "nameKo": "피나 콜라다",
                      "nameEn": "Pina Colada",
                      "shortDescription": "상큼한 풍미 산뜻함에 진한 달콤함을 더한 깔끔한 밸런스의 투명한 뒷맛 칵테일",
                      "imageUrl": "https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/PINA%20COLADA.png",
                      "unlocked": false
                    },
                    {
                      "cocktailId": 15,
                      "nameKo": "포른 스타 마티니",
                      "nameEn": "Porn Star Martini",
                      "shortDescription": "묵직한 단맛 베이스에 톡 쏘는 신맛과 은근한 뒷맛이 어우러진 목 넘김이 편한 칵테일",
                      "imageUrl": "https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/PORN%20STAR%20MARTINI.png",
                      "unlocked": false
                    },
                    {
                      "cocktailId": 16,
                      "nameKo": "러시안 스프링 펀치",
                      "nameEn": "Russian Spring Punch",
                      "shortDescription": "부담 없이 가벼운 보드카 베이스로, 녹진한 단맛과 짜릿한 산미가 조화를 이룬 칵테일",
                      "imageUrl": "https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/cocktails/RUSSIAN%20SPRING%20PUNCH.png",
                      "unlocked": false
                    }
                  ],
                  "totalCocktailCount": 8,
                  "unlockedCocktailCount": 0
                }
              }
              """;

    String COMMON401_EXAMPLE = """
          {
            "timestamp": "2026-08-13T14:30:00",
            "code": "COMMON401",
            "message": "인증이 필요합니다."
          }
          """;

    String AUTH010_EXAMPLE = """
          {
            "timestamp": "2026-08-13T14:30:00",
            "code": "AUTH010",
            "message": "존재하지 않는 사용자입니다."
          }
          """;

    String MOOD_TYPE404_EXAMPLE = """
          {
            "timestamp": "2026-08-13T14:30:00",
            "code": "MOOD_TYPE404",
            "message": "해당 칵테일 타입을 찾을 수 없습니다."
          }
          """;

    String COMMON405_EXAMPLE = """
          {
            "timestamp": "2026-08-13T14:30:00",
            "code": "COMMON405",
            "message": "요청 인자 타입이 올바르지 않습니다."
          }
          """;

    String AUTH006_EXAMPLE = """
          {
            "timestamp": "2026-08-13T14:30:00",
            "code": "AUTH006",
            "message": "유효하지 않은 액세스 토큰입니다."
          }
          """;

    String AUTH009_EXAMPLE = """
          {
            "timestamp": "2026-08-13T14:30:00",
            "code": "AUTH009",
            "message": "권한이 없습니다."
          }
          """;

    String AUTH020_EXAMPLE = """
          {
            "timestamp": "2026-08-13T14:30:00",
            "code": "AUTH020",
            "message": "비활성화된 사용자입니다."
          }
          """;

    String AUTH027_EXAMPLE = """
          {
            "timestamp": "2026-08-13T14:30:00",
            "code": "AUTH027",
            "message": "기록을 저장하려면 로그인하세요."
          }
          """;

    String COMMON500_EXAMPLE = """
          {
            "timestamp": "2026-08-13T14:30:00",
            "code": "COMMON500",
            "message": "서버 에러가 발생했습니다."
          }
          """;

    String AUTH028_EXAMPLE = """
          {
            "timestamp": "2026-08-13T14:30:00",
            "code": "AUTH028",
            "message": "인증 서비스를 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도해주세요."
          }
          """;

    @Operation(
            operationId = "getMoodType",
            summary = "도감 무드 타입 상세 조회",
            description = """
                      무드 타입의 기본 정보와 성향 수치, 궁합 정보를 조회합니다.
                      사용자의 해금 여부, 대표 타입 여부, 수집률과 칵테일 목록을 함께 반환합니다.
                      """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "무드 타입 상세 조회 성공",
                    useReturnTypeSchema = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "COMMON200",
                                    value = MOOD_TYPE_SUCCESS_EXAMPLE
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "COMMON405 - moodTypeId 타입 오류",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "COMMON405",
                                    value = COMMON405_EXAMPLE
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = """
                          COMMON401 - 인증 정보 없음
                          AUTH006 - 유효하지 않은 액세스 토큰
                          AUTH010 - 존재하지 않는 사용자
                          """,
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "COMMON401",
                                            value = COMMON401_EXAMPLE
                                    ),
                                    @ExampleObject(
                                            name = "AUTH006",
                                            value = AUTH006_EXAMPLE
                                    ),
                                    @ExampleObject(
                                            name = "AUTH010",
                                            value = AUTH010_EXAMPLE
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = """
                          AUTH009 - 권한 정보가 올바르지 않음
                          AUTH020 - 비활성 사용자
                          AUTH027 - 게스트 사용자 접근 불가
                          """,
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "AUTH009",
                                            value = AUTH009_EXAMPLE
                                    ),
                                    @ExampleObject(
                                            name = "AUTH020",
                                            value = AUTH020_EXAMPLE
                                    ),
                                    @ExampleObject(
                                            name = "AUTH027",
                                            value = AUTH027_EXAMPLE
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "요청한 무드 타입이 존재하지 않음",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "MOOD_TYPE404",
                                    value = MOOD_TYPE404_EXAMPLE
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
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "AUTH028 - 인증 서비스 일시 장애",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "AUTH028",
                                    value = AUTH028_EXAMPLE
                            )
                    )
            )
    })
    BaseResponse<MoodTypeResponse> getMoodType(
            @Parameter(
                    description = "조회할 무드 타입 ID",
                    example = "1",
                    required = true
            )
            Long moodTypeId,

            @Parameter(hidden = true)
            PrincipalDetails principalDetails
    );
}
