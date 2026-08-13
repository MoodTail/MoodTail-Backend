package com.example.moodtail.domain.report.controller.docs;

import com.example.moodtail.domain.report.dto.response.MonthlyReportResponse;
import com.example.moodtail.domain.report.dto.response.MonthlyReportShareImageResponse;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Reports", description = "회원의 월간 리포트 조회 및 공유 URL 생성 API")
@SecurityRequirement(name = "bearerAuth")
public interface MonthlyReportControllerDocs {

    String MONTHLY_REPORT_SUCCESS_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "COMMON200",
              "message": "요청에 성공했습니다.",
              "result": {
                "year": 2026,
                "month": 7,
                "monthlyMoodType": {
                  "moodTypeId": 2001,
                  "typeCode": "TYPE01",
                  "name": "몽글몽글 낭만파",
                  "shortDescription": "부드러운 달콤함 속에서 여유를 즐기는 타입",
                  "characterQuote": "천천히 즐겨도 괜찮아요.",
                  "characterImageUrl": "https://cdn.moodtail.com/mood-types/type01.png"
                },
                "topMoodTypes": [
                  {
                    "moodTypeId": 2001,
                    "typeCode": "TYPE01",
                    "name": "몽글몽글 낭만파",
                    "characterImageUrl": "https://cdn.moodtail.com/mood-types/type01.png",
                    "count": 2,
                    "ranking": 1
                  },
                  {
                    "moodTypeId": 2002,
                    "typeCode": "TYPE02",
                    "name": "반짝이는 모험가",
                    "characterImageUrl": "https://cdn.moodtail.com/mood-types/type02.png",
                    "count": 2,
                    "ranking": 1
                  },
                  {
                    "moodTypeId": 2003,
                    "typeCode": "TYPE03",
                    "name": "차분한 사색가",
                    "characterImageUrl": "https://cdn.moodtail.com/mood-types/type03.png",
                    "count": 1,
                    "ranking": 3
                  }
                ],
                "averageTasteProfile": {
                  "alcoholIntensity": 2.8,
                  "sweetness": 4.1,
                  "sourness": 2.5,
                  "refreshing": 3.8,
                  "bitterness": 1.7
                },
                "displayAverageTasteScores": {
                  "alcoholIntensity": 45,
                  "sweetness": 78,
                  "sourness": 38,
                  "refreshing": 70,
                  "bitterness": 18
                },
                "previousMonthTasteProfile": null,
                "previousMonthDisplayTasteScores": null,
                "frequentCocktails": [
                  {
                    "cocktailId": 10,
                    "nameKo": "모히또",
                    "nameEn": "Mojito",
                    "shortDescription": "상쾌한 민트와 라임의 조화",
                    "imageUrl": "https://cdn.moodtail.com/cocktails/mojito.png",
                    "count": 2,
                    "recordPercentage": 40,
                    "ranking": 1
                  },
                  {
                    "cocktailId": 11,
                    "nameKo": "보드카 토닉",
                    "nameEn": "Vodka Tonic",
                    "shortDescription": "깔끔한 보드카와 토닉워터의 조화",
                    "imageUrl": "https://cdn.moodtail.com/cocktails/vodka-tonic.png",
                    "count": 2,
                    "recordPercentage": 40,
                    "ranking": 1
                  },
                  {
                    "cocktailId": 12,
                    "nameKo": "진 토닉",
                    "nameEn": "Gin Tonic",
                    "shortDescription": "향긋한 진과 토닉워터의 조화",
                    "imageUrl": "https://cdn.moodtail.com/cocktails/gin-tonic.png",
                    "count": 1,
                    "recordPercentage": 20,
                    "ranking": 3
                  }
                ],
                "activity": {
                  "testCount": 5,
                  "drinkingRecordCount": 5
                }
              }
            }
            """;
    String SHARE_IMAGE_SUCCESS_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "COMMON200",
              "message": "요청에 성공했습니다.",
              "result": {
                "shareToken": "mr_hJ7JngQmYV4x0aP9k2LmN3Qr",
                "shareUrl": "https://mood-tail.site/share/reports/monthly/mr_hJ7JngQmYV4x0aP9k2LmN3Qr"
              }
            }
            """;
    String COMMON401_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "COMMON401",
              "message": "인증이 필요합니다."
            }
            """;
    String COMMON402_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "COMMON402",
              "message": "입력값 검증에 실패했습니다."
            }
            """;
    String COMMON405_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "COMMON405",
              "message": "요청 인자 타입이 올바르지 않습니다."
            }
            """;
    String COMMON500_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "COMMON500",
              "message": "서버 에러가 발생했습니다."
            }
            """;
    String AUTH006_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "AUTH006",
              "message": "유효하지 않은 액세스 토큰입니다."
            }
            """;
    String AUTH009_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "AUTH009",
              "message": "권한이 없습니다."
            }
            """;
    String AUTH010_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "AUTH010",
              "message": "존재하지 않는 사용자입니다."
            }
            """;
    String AUTH020_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "AUTH020",
              "message": "비활성화된 사용자입니다."
            }
            """;
    String AUTH027_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "AUTH027",
              "message": "기록을 저장하려면 로그인하세요"
            }
            """;
    String AUTH028_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "AUTH028",
              "message": "인증 서비스를 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도해주세요."
            }
            """;
    String REPORT400_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "REPORT400",
              "message": "월간 리포트 요청 값이 올바르지 않습니다."
            }
            """;
    String REPORT409_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "REPORT409",
              "message": "월간 리포트 생성에 필요한 데이터가 부족합니다."
            }
            """;
    String REPORT_IMAGE503_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "REPORT_IMAGE503",
              "message": "월간 리포트 공유 이미지를 일시적으로 저장할 수 없습니다."
            }
            """;
    String IMAGE400_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "IMAGE400",
              "message": "이미지 파일 형식이 올바르지 않습니다."
            }
            """;
    String IMAGE413_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "IMAGE413",
              "message": "이미지 파일은 5MB 이하여야 합니다."
            }
            """;

    @Operation(
            operationId = "getMonthlyReport",
            summary = "월간 리포트 조회",
            description = "지정 월의 테스트 결과와 음주 기록을 집계합니다. 해당 월 테스트 결과가 5건 "
                    + "이상이어야 하며 미래 월은 조회할 수 없습니다. topMoodTypes는 1~3개, "
                    + "frequentCocktails는 0~3개이고 공동 순위는 1, 1, 3처럼 반환될 수 있습니다. "
                    + "frequentCocktails의 recordPercentage는 해당 칵테일 기록 수를 그 달의 전체 음주 기록 "
                    + "수로 나눈 뒤 반올림한 정수 비율이며, count와 함께 반환됩니다. 항목별 반올림과 "
                    + "상위 3개 밖의 기록도 분모에 포함되므로 목록의 비율 합계는 정확히 100이 아닐 수 있습니다. "
                    + "monthlyMoodType은 대표 카드의 이미지·이름·두 문구를 제공하고, 동일 moodTypeId인 "
                    + "topMoodTypes 항목에서 대표 타입의 순위와 횟수를 확인할 수 있습니다. "
                    + "맛 원본 점수는 1.0~5.0, display 점수는 0~100입니다. 이전 달 테스트 결과가 없으면 "
                    + "previousMonthTasteProfile과 previousMonthDisplayTasteScores는 null입니다. "
                    + "이미지 URL도 등록 상태에 따라 null일 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 월간 리포트 조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = ReportApiResponseSchemas.MonthlyReport.class),
                            examples = @ExampleObject(
                                    name = "COMMON200",
                                    value = MONTHLY_REPORT_SUCCESS_EXAMPLE
                            )
                    )),
            @ApiResponse(responseCode = "400",
                    description = """
                            COMMON402 - year 또는 month 필수 요청 값 누락
                            COMMON405 - year 또는 month 타입 변환 실패
                            REPORT400 - 지원 범위 밖의 연도·월 또는 미래 월 요청
                            """,
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "COMMON405", value = COMMON405_EXAMPLE),
                            @ExampleObject(name = "REPORT400", value = REPORT400_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "401",
                    description = """
                            COMMON401 - 인증 정보 없음
                            AUTH006 - 유효하지 않거나 만료된 Access Token
                            AUTH010 - 토큰의 사용자를 찾을 수 없음
                            """,
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON401", value = COMMON401_EXAMPLE),
                            @ExampleObject(name = "AUTH006", value = AUTH006_EXAMPLE),
                            @ExampleObject(name = "AUTH010", value = AUTH010_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "403",
                    description = """
                            AUTH009 - 회원 역할이 아닌 사용자
                            AUTH020 - 비활성 또는 탈퇴 사용자
                            AUTH027 - 게스트 사용자 접근
                            """,
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH009", value = AUTH009_EXAMPLE),
                            @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE),
                            @ExampleObject(name = "AUTH027", value = AUTH027_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "409", description = "REPORT409 - 해당 월 테스트 결과 5건 미만",
                    content = @Content(examples = @ExampleObject(name = "REPORT409", value = REPORT409_EXAMPLE))),
            @ApiResponse(responseCode = "503", description = "AUTH028 - 인증 저장소 일시 장애",
                    content = @Content(examples = @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    BaseResponse<MonthlyReportResponse> getMonthlyReport(
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(
                    description = "조회 연도(1000 이상, 미래 월 조회 불가)",
                    required = true,
                    schema = @Schema(minimum = "1000"),
                    example = "2026"
            ) int year,
            @Parameter(
                    description = "조회 월(1~12)",
                    required = true,
                    schema = @Schema(minimum = "1", maximum = "12"),
                    example = "7"
            ) int month
    );

    @Operation(
            operationId = "createMonthlyReportShareImage",
            summary = "월간 리포트 공유 URL 생성",
            description = """
                    프론트엔드에서 월간 리포트 데이터로 생성한 이미지를 S3의
                    `public/reports/monthly` 경로에 저장하고 공유 토큰과 공개 공유 URL을 반환합니다.

                    - 이미지는 5MB 이하의 JPG, PNG 또는 WEBP 형식만 허용합니다.
                    - 파일 확장자·Content-Type·Magic Bytes가 실제 이미지 형식과 일치해야 합니다.
                    - 조회 API와 동일하게 해당 월 테스트 결과가 5건 이상이어야 합니다.
                    - 공유 토큰은 생성 후 30일 동안 조회할 수 있습니다.
                    - 이미지는 객체 생성 30일 경과 후 S3 수명 주기 정책이 실행되는 시점에 삭제됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 월간 리포트 공유 URL 생성 성공",
                    content = @Content(
                            schema = @Schema(implementation = ReportApiResponseSchemas.ShareImage.class),
                            examples = @ExampleObject(
                                    name = "COMMON200",
                                    value = SHARE_IMAGE_SUCCESS_EXAMPLE
                            )
                    )),
            @ApiResponse(responseCode = "400",
                    description = """
                            COMMON402 - year·month 또는 image 필수 요청 값 누락
                            COMMON405 - year 또는 month 타입 변환 실패
                            REPORT400 - 지원 범위 밖의 연도·월 또는 미래 월 요청
                            IMAGE400 - 빈 파일, 파일 확장자·Content-Type 불일치 또는
                            Magic Bytes가 PNG, JPEG, WEBP 형식과 일치하지 않음
                            """,
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "COMMON405", value = COMMON405_EXAMPLE),
                            @ExampleObject(name = "REPORT400", value = REPORT400_EXAMPLE),
                            @ExampleObject(name = "IMAGE400", value = IMAGE400_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "401",
                    description = """
                            COMMON401 - 인증 정보 없음
                            AUTH006 - 유효하지 않거나 만료된 Access Token
                            AUTH010 - 토큰의 사용자를 찾을 수 없음
                            """,
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON401", value = COMMON401_EXAMPLE),
                            @ExampleObject(name = "AUTH006", value = AUTH006_EXAMPLE),
                            @ExampleObject(name = "AUTH010", value = AUTH010_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "403",
                    description = """
                            AUTH009 - 회원 역할이 아닌 사용자
                            AUTH020 - 비활성 또는 탈퇴 사용자
                            AUTH027 - 게스트 사용자 접근
                            """,
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH009", value = AUTH009_EXAMPLE),
                            @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE),
                            @ExampleObject(name = "AUTH027", value = AUTH027_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "409", description = "REPORT409 - 해당 월 테스트 결과 5건 미만",
                    content = @Content(examples = @ExampleObject(name = "REPORT409", value = REPORT409_EXAMPLE))),
            @ApiResponse(responseCode = "413", description = "IMAGE413 - 이미지 파일 5MB 초과",
                    content = @Content(examples = @ExampleObject(name = "IMAGE413", value = IMAGE413_EXAMPLE))),
            @ApiResponse(responseCode = "503",
                    description = """
                            AUTH028 - 인증 저장소 일시 장애
                            REPORT_IMAGE503 - 월간 리포트 공유 이미지 저장소 일시 장애
                            """,
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE),
                            @ExampleObject(name = "REPORT_IMAGE503", value = REPORT_IMAGE503_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    BaseResponse<MonthlyReportShareImageResponse> createMonthlyReportShareImage(
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(
                    description = "리포트 연도(1000 이상, 미래 월 불가)",
                    required = true,
                    schema = @Schema(minimum = "1000"),
                    example = "2026"
            ) int year,
            @Parameter(
                    description = "리포트 월(1~12)",
                    required = true,
                    schema = @Schema(minimum = "1", maximum = "12"),
                    example = "7"
            ) int month,
            @Parameter(
                    description = "프론트엔드에서 생성한 5MB 이하의 JPG, PNG 또는 WEBP 이미지. "
                            + "파일 확장자·Content-Type·Magic Bytes가 실제 형식과 일치해야 합니다.",
                    required = true,
                    schema = @Schema(types = {"string"}, format = "binary")
            )
            MultipartFile image
    );
}
