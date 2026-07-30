package com.example.moodtail.domain.history.controller.docs;

import com.example.moodtail.domain.history.dto.request.HistoryCreateRequest;
import com.example.moodtail.domain.history.dto.request.HistoryUpdateRequest;
import com.example.moodtail.domain.history.dto.response.HistoryCalendarResponse;
import com.example.moodtail.domain.history.dto.response.HistoryCreateResponse;
import com.example.moodtail.domain.history.dto.response.HistoryDateResponse;
import com.example.moodtail.domain.history.dto.response.HistoryDetailResponse;
import com.example.moodtail.domain.history.dto.response.HistoryPhotoResponse;
import com.example.moodtail.domain.history.dto.response.HistoryTestResultDetailResponse;
import com.example.moodtail.domain.history.dto.response.HistoryUpdateResponse;
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

@Tag(name = "Histories", description = "회원의 월간·날짜별 히스토리, 음주 기록 및 사진 API")
@SecurityRequirement(name = "bearerAuth")
public interface HistoryControllerDocs {

    String CALENDAR_SUCCESS_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "COMMON200",
              "message": "요청에 성공했습니다.",
              "result": {
                "year": 2026,
                "month": 7,
                "testResultCount": 6,
                "drinkingRecordCount": 3,
                "reportRequiredTestCount": 5,
                "reportAvailable": true,
                "testResults": [
                  {
                    "resultId": 71,
                    "resultDate": "2026-07-05",
                    "moodType": {
                      "moodTypeId": 2001,
                      "typeCode": "TYPE01",
                      "name": "몽글몽글 낭만파",
                      "characterImageUrl": "https://cdn.moodtail.com/mood-types/type01.png"
                    }
                  }
                ],
                "days": [
                  {
                    "date": "2026-07-05",
                    "hasTestResult": true,
                    "hasDrinkingRecord": true,
                    "photoCount": 1,
                    "moodType": {
                      "moodTypeId": 2001,
                      "typeCode": "TYPE01",
                      "name": "몽글몽글 낭만파",
                      "characterImageUrl": "https://cdn.moodtail.com/mood-types/type01.png"
                    }
                  },
                  {
                    "date": "2026-07-10",
                    "hasTestResult": false,
                    "hasDrinkingRecord": true,
                    "photoCount": 0,
                    "moodType": null
                  },
                  {
                    "date": "2026-07-15",
                    "hasTestResult": false,
                    "hasDrinkingRecord": false,
                    "photoCount": 2,
                    "moodType": null
                  }
                ]
              }
            }
            """;
    String DATE_SUCCESS_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "COMMON200",
              "message": "요청에 성공했습니다.",
              "result": {
                "date": "2026-07-05",
                "testResult": {
                  "resultId": 71,
                  "moodType": {
                    "moodTypeId": 2001,
                    "typeCode": "TYPE01",
                    "name": "몽글몽글 낭만파",
                    "shortDescription": "부드러운 달콤함 속에서 여유를 즐기는 타입",
                    "characterImageUrl": "https://cdn.moodtail.com/mood-types/type01.png"
                  }
                },
                "drinkingRecords": [
                  {
                    "recordId": 31,
                    "cocktailId": 10,
                    "cocktailName": "모히또",
                    "shortDescription": "민트와 라임의 청량한 만남",
                    "alcoholDegree": 20.0,
                    "cocktailImageUrl": "https://cdn.moodtail.com/cocktails/mojito.png"
                  },
                  {
                    "recordId": 32,
                    "cocktailId": 11,
                    "cocktailName": "마가리타",
                    "shortDescription": null,
                    "alcoholDegree": null,
                    "cocktailImageUrl": null
                  }
                ],
                "photos": [
                  {
                    "photoId": 3,
                    "imageUrl": "https://cdn.moodtail.com/histories/37/2026-07-05/photo.jpg"
                  }
                ]
              }
            }
            """;
    String TEST_RESULT_DETAIL_SUCCESS_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "COMMON200",
              "message": "요청에 성공했습니다.",
              "result": {
                "resultId": 71,
                "resultDate": "2026-07-05",
                "moodType": {
                  "moodTypeId": 2001,
                  "typeCode": "TYPE01",
                  "name": "몽글몽글 낭만파",
                  "shortDescription": "부드러운 달콤함 속에서 여유를 즐기는 타입",
                  "description": "오늘은 잔잔한 여유가 어울리는 날이에요.",
                  "characterQuote": "천천히 즐겨도 괜찮아요.",
                  "characterImageUrl": "https://cdn.moodtail.com/mood-types/type01.png"
                },
                "tasteProfile": {
                  "alcoholIntensity": 2.8,
                  "sweetness": 4.1,
                  "sourness": 2.5,
                  "refreshing": 3.8,
                  "bitterness": 1.7
                },
                "recommendedCocktails": [
                  {
                    "cocktailId": 10,
                    "cocktailName": "모히또",
                    "cocktailImageUrl": "https://cdn.moodtail.com/cocktails/mojito.png",
                    "ranking": 1,
                    "matchScore": 92
                  }
                ]
              }
            }
            """;
    String DRINKING_RECORD_DETAIL_SUCCESS_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "COMMON200",
              "message": "요청에 성공했습니다.",
              "result": {
                "recordId": 31,
                "cocktailId": 10,
                "cocktailName": "모히또",
                "cocktailImageUrl": "https://cdn.moodtail.com/cocktails/mojito.png",
                "recordDate": "2026-07-05"
              }
            }
            """;
    String DRINKING_RECORD_CREATE_SUCCESS_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "COMMON200",
              "message": "요청에 성공했습니다.",
              "result": {
                "recordId": 31,
                "recordDate": "2026-07-05"
              }
            }
            """;
    String DRINKING_RECORD_UPDATE_SUCCESS_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "COMMON200",
              "message": "요청에 성공했습니다.",
              "result": {
                "recordId": 31
              }
            }
            """;
    String PHOTO_CREATE_SUCCESS_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "COMMON200",
              "message": "요청에 성공했습니다.",
              "result": {
                "photoId": 3,
                "recordDate": "2026-07-05",
                "imageUrl": "https://cdn.moodtail.com/histories/37/2026-07-05/photo.jpg"
              }
            }
            """;
    String VOID_SUCCESS_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "COMMON200",
              "message": "요청에 성공했습니다."
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
    String COMMON406_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "COMMON406",
              "message": "요청 본문 형식이 올바르지 않습니다."
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
    String HISTORY400_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "HISTORY400",
              "message": "히스토리 요청 값이 올바르지 않습니다."
            }
            """;
    String HISTORY409_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "HISTORY409",
              "message": "해당 날짜에 같은 칵테일 음주 기록이 이미 있습니다."
            }
            """;
    String HISTORY404_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "HISTORY404",
              "message": "히스토리 기록을 찾을 수 없습니다."
            }
            """;
    String HISTORY_TEST404_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "HISTORY_TEST404",
              "message": "저장된 테스트 결과를 찾을 수 없습니다."
            }
            """;
    String HISTORY_PHOTO409_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "HISTORY_PHOTO409",
              "message": "날짜별 사진은 최대 5장까지 저장할 수 있습니다."
            }
            """;
    String HISTORY_PHOTO404_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "HISTORY_PHOTO404",
              "message": "히스토리 사진을 찾을 수 없습니다."
            }
            """;
    String HISTORY_PHOTO503_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "HISTORY_PHOTO503",
              "message": "사진 저장소를 일시적으로 사용할 수 없습니다."
            }
            """;
    String COCKTAIL404_EXAMPLE = """
            {
              "timestamp": "2026-07-24T14:30:00",
              "code": "COCKTAIL404",
              "message": "해당 칵테일을 찾을 수 없습니다."
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
            operationId = "getHistoryCalendar",
            summary = "월간 히스토리 조회",
            description = "지정한 월의 테스트 결과, 음주 기록 존재 여부, 날짜별 사진 개수와 월간 리포트 열람 "
                    + "가능 여부를 조회합니다. 미래 월은 조회할 수 없습니다. days에는 테스트 결과, 음주 기록 "
                    + "또는 사진이 하나 이상 존재하는 날짜가 포함되며, 사진만 있는 날짜도 반환됩니다. 음주 "
                    + "기록의 칵테일 상세 목록은 날짜별 히스토리 API에서 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 월간 히스토리 조회 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = CALENDAR_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "400",
                    description = "COMMON402/COMMON405/HISTORY400 - 누락·타입 오류 또는 유효하지 않은 연도·월",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "COMMON405", value = COMMON405_EXAMPLE),
                            @ExampleObject(name = "HISTORY400", value = HISTORY400_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "401",
                    description = "COMMON401/AUTH006/AUTH010 - 인증 토큰 또는 사용자 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON401", value = COMMON401_EXAMPLE),
                            @ExampleObject(name = "AUTH006", value = AUTH006_EXAMPLE),
                            @ExampleObject(name = "AUTH010", value = AUTH010_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "403",
                    description = "AUTH009/AUTH020/AUTH027 - 회원 권한 또는 사용자 상태 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH009", value = AUTH009_EXAMPLE),
                            @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE),
                            @ExampleObject(name = "AUTH027", value = AUTH027_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "503", description = "AUTH028 - 인증 저장소 일시 장애",
                    content = @Content(examples = @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    BaseResponse<HistoryCalendarResponse> getHistoryCalendar(
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(description = "조회 연도(1000 이상, 미래 월 조회 불가)", example = "2026") int year,
            @Parameter(description = "조회 월(1~12)", example = "7") int month
    );

    @Operation(
            operationId = "getHistoryByDate",
            summary = "날짜별 히스토리 조회",
            description = "선택 날짜의 테스트 결과 요약, 서로 다른 칵테일 음주 기록 목록과 사진 목록을 "
                    + "조회합니다. 데이터가 없는 날짜도 빈 목록과 null 테스트 결과로 COMMON200을 반환하며 "
                    + "미래 날짜는 조회할 수 없습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 날짜별 히스토리 조회 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = DATE_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "400",
                    description = "HISTORY400 - 날짜 형식 오류, MySQL 지원 범위 밖 또는 미래 날짜",
                    content = @Content(examples = @ExampleObject(name = "HISTORY400", value = HISTORY400_EXAMPLE))),
            @ApiResponse(responseCode = "401",
                    description = "COMMON401/AUTH006/AUTH010 - 인증 토큰 또는 사용자 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON401", value = COMMON401_EXAMPLE),
                            @ExampleObject(name = "AUTH006", value = AUTH006_EXAMPLE),
                            @ExampleObject(name = "AUTH010", value = AUTH010_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "403",
                    description = "AUTH009/AUTH020/AUTH027 - 회원 권한 또는 사용자 상태 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH009", value = AUTH009_EXAMPLE),
                            @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE),
                            @ExampleObject(name = "AUTH027", value = AUTH027_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "503", description = "AUTH028 - 인증 저장소 일시 장애",
                    content = @Content(examples = @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    BaseResponse<HistoryDateResponse> getHistoryByDate(
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(description = "조회 날짜(yyyy-MM-dd)", example = "2026-07-05") String date
    );

    @Operation(
            operationId = "getTestResultDetail",
            summary = "저장된 테스트 결과 상세 조회",
            description = "본인의 테스트 결과에 저장된 무드 타입, 1~5 맛 프로필과 당시 추천 칵테일을 "
                    + "조회합니다. 존재하지 않거나 다른 회원 소유인 결과는 동일하게 HISTORY_TEST404를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 테스트 결과 상세 조회 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(
                            name = "COMMON200",
                            value = TEST_RESULT_DETAIL_SUCCESS_EXAMPLE
                    ))),
            @ApiResponse(responseCode = "400", description = "HISTORY400 - 0 이하의 테스트 결과 ID",
                    content = @Content(examples = @ExampleObject(name = "HISTORY400", value = HISTORY400_EXAMPLE))),
            @ApiResponse(responseCode = "401",
                    description = "COMMON401/AUTH006/AUTH010 - 인증 토큰 또는 사용자 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON401", value = COMMON401_EXAMPLE),
                            @ExampleObject(name = "AUTH006", value = AUTH006_EXAMPLE),
                            @ExampleObject(name = "AUTH010", value = AUTH010_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "403",
                    description = "AUTH009/AUTH020/AUTH027 - 회원 권한 또는 사용자 상태 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH009", value = AUTH009_EXAMPLE),
                            @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE),
                            @ExampleObject(name = "AUTH027", value = AUTH027_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "404", description = "HISTORY_TEST404 - 결과 없음 또는 다른 회원 소유",
                    content = @Content(examples = @ExampleObject(
                            name = "HISTORY_TEST404",
                            value = HISTORY_TEST404_EXAMPLE
                    ))),
            @ApiResponse(responseCode = "503", description = "AUTH028 - 인증 저장소 일시 장애",
                    content = @Content(examples = @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    BaseResponse<HistoryTestResultDetailResponse> getTestResultDetail(
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(description = "테스트 결과 ID(양수)", example = "71") Long resultId
    );

    @Operation(
            operationId = "getHistoryDetail",
            summary = "음주 기록 상세 조회",
            description = "본인의 음주 기록에 연결된 칵테일과 기록 날짜를 조회합니다. 존재하지 않거나 다른 "
                    + "회원 소유인 기록은 동일하게 HISTORY404를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 음주 기록 상세 조회 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(
                            name = "COMMON200",
                            value = DRINKING_RECORD_DETAIL_SUCCESS_EXAMPLE
                    ))),
            @ApiResponse(responseCode = "400", description = "HISTORY400 - 0 이하의 음주 기록 ID",
                    content = @Content(examples = @ExampleObject(name = "HISTORY400", value = HISTORY400_EXAMPLE))),
            @ApiResponse(responseCode = "401",
                    description = "COMMON401/AUTH006/AUTH010 - 인증 토큰 또는 사용자 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON401", value = COMMON401_EXAMPLE),
                            @ExampleObject(name = "AUTH006", value = AUTH006_EXAMPLE),
                            @ExampleObject(name = "AUTH010", value = AUTH010_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "403",
                    description = "AUTH009/AUTH020/AUTH027 - 회원 권한 또는 사용자 상태 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH009", value = AUTH009_EXAMPLE),
                            @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE),
                            @ExampleObject(name = "AUTH027", value = AUTH027_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "404", description = "HISTORY404 - 기록 없음 또는 다른 회원 소유",
                    content = @Content(examples = @ExampleObject(name = "HISTORY404", value = HISTORY404_EXAMPLE))),
            @ApiResponse(responseCode = "503", description = "AUTH028 - 인증 저장소 일시 장애",
                    content = @Content(examples = @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    BaseResponse<HistoryDetailResponse> getHistoryDetail(
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(description = "음주 기록 ID(양수)", example = "31") Long recordId
    );

    @Operation(
            operationId = "createHistory",
            summary = "음주 기록 생성",
            description = "선택 날짜에 마신 칵테일을 기록합니다. 같은 날짜에 서로 다른 칵테일은 여러 건 "
                    + "기록할 수 있지만 같은 칵테일은 중복 등록할 수 없습니다. 미래 날짜는 기록할 수 없습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 음주 기록 생성 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(
                            name = "COMMON200",
                            value = DRINKING_RECORD_CREATE_SUCCESS_EXAMPLE
                    ))),
            @ApiResponse(responseCode = "400",
                    description = "COMMON402/COMMON406/HISTORY400 - 필드·JSON·날짜 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "COMMON406", value = COMMON406_EXAMPLE),
                            @ExampleObject(name = "HISTORY400", value = HISTORY400_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "401",
                    description = "COMMON401/AUTH006/AUTH010 - 인증 토큰 또는 사용자 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON401", value = COMMON401_EXAMPLE),
                            @ExampleObject(name = "AUTH006", value = AUTH006_EXAMPLE),
                            @ExampleObject(name = "AUTH010", value = AUTH010_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "403",
                    description = "AUTH009/AUTH020/AUTH027 - 회원 권한 또는 사용자 상태 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH009", value = AUTH009_EXAMPLE),
                            @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE),
                            @ExampleObject(name = "AUTH027", value = AUTH027_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "404", description = "COCKTAIL404 - 칵테일 없음",
                    content = @Content(examples = @ExampleObject(name = "COCKTAIL404", value = COCKTAIL404_EXAMPLE))),
            @ApiResponse(responseCode = "409", description = "HISTORY409 - 같은 날짜·칵테일 기록 중복",
                    content = @Content(examples = @ExampleObject(name = "HISTORY409", value = HISTORY409_EXAMPLE))),
            @ApiResponse(responseCode = "503", description = "AUTH028 - 인증 저장소 일시 장애",
                    content = @Content(examples = @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    BaseResponse<HistoryCreateResponse> createHistory(
            @Parameter(hidden = true) PrincipalDetails principal,
            HistoryCreateRequest request
    );

    @Operation(
            operationId = "updateHistory",
            summary = "음주 기록 수정",
            description = "칵테일 또는 기록 날짜 중 변경할 필드만 전달합니다. 두 필드를 모두 생략하거나 미래 "
                    + "날짜로 변경할 수 없습니다. 변경 결과가 같은 날짜의 다른 동일 칵테일 기록과 중복되면 "
                    + "HISTORY409를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 음주 기록 수정 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(
                            name = "COMMON200",
                            value = DRINKING_RECORD_UPDATE_SUCCESS_EXAMPLE
                    ))),
            @ApiResponse(responseCode = "400",
                    description = "COMMON402/COMMON406/HISTORY400 - ID·JSON·빈 수정 요청·날짜 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "COMMON406", value = COMMON406_EXAMPLE),
                            @ExampleObject(name = "HISTORY400", value = HISTORY400_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "401",
                    description = "COMMON401/AUTH006/AUTH010 - 인증 토큰 또는 사용자 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON401", value = COMMON401_EXAMPLE),
                            @ExampleObject(name = "AUTH006", value = AUTH006_EXAMPLE),
                            @ExampleObject(name = "AUTH010", value = AUTH010_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "403",
                    description = "AUTH009/AUTH020/AUTH027 - 회원 권한 또는 사용자 상태 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH009", value = AUTH009_EXAMPLE),
                            @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE),
                            @ExampleObject(name = "AUTH027", value = AUTH027_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "404", description = "HISTORY404/COCKTAIL404 - 기록 또는 칵테일 없음",
                    content = @Content(examples = {
                            @ExampleObject(name = "HISTORY404", value = HISTORY404_EXAMPLE),
                            @ExampleObject(name = "COCKTAIL404", value = COCKTAIL404_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "409", description = "HISTORY409 - 변경 후 날짜·칵테일 중복",
                    content = @Content(examples = @ExampleObject(name = "HISTORY409", value = HISTORY409_EXAMPLE))),
            @ApiResponse(responseCode = "503", description = "AUTH028 - 인증 저장소 일시 장애",
                    content = @Content(examples = @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    BaseResponse<HistoryUpdateResponse> updateHistory(
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(description = "수정할 음주 기록 ID(양수)", example = "31") Long recordId,
            HistoryUpdateRequest request
    );

    @Operation(
            operationId = "deleteHistory",
            summary = "음주 기록 삭제",
            description = "본인의 음주 기록 한 건을 삭제합니다. 존재하지 않거나 다른 회원 소유인 기록은 "
                    + "동일하게 HISTORY404를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 음주 기록 삭제 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = VOID_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "HISTORY400 - 0 이하의 음주 기록 ID",
                    content = @Content(examples = @ExampleObject(name = "HISTORY400", value = HISTORY400_EXAMPLE))),
            @ApiResponse(responseCode = "401",
                    description = "COMMON401/AUTH006/AUTH010 - 인증 토큰 또는 사용자 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON401", value = COMMON401_EXAMPLE),
                            @ExampleObject(name = "AUTH006", value = AUTH006_EXAMPLE),
                            @ExampleObject(name = "AUTH010", value = AUTH010_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "403",
                    description = "AUTH009/AUTH020/AUTH027 - 회원 권한 또는 사용자 상태 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH009", value = AUTH009_EXAMPLE),
                            @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE),
                            @ExampleObject(name = "AUTH027", value = AUTH027_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "404", description = "HISTORY404 - 기록 없음 또는 다른 회원 소유",
                    content = @Content(examples = @ExampleObject(name = "HISTORY404", value = HISTORY404_EXAMPLE))),
            @ApiResponse(responseCode = "503", description = "AUTH028 - 인증 저장소 일시 장애",
                    content = @Content(examples = @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    BaseResponse<Void> deleteHistory(
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(description = "삭제할 음주 기록 ID(양수)", example = "31") Long recordId
    );

    @Operation(
            operationId = "addHistoryPhoto",
            summary = "날짜별 히스토리 사진 추가",
            description = """
                    `multipart/form-data` 요청으로 선택 날짜에 사진 한 장을 추가합니다.

                    - `date`: 사진 기록 날짜(`yyyy-MM-dd`, 미래 날짜 불가)
                    - `image`: JPG, PNG 또는 WEBP 형식의 5MB 이하 이미지
                    - 사용자별 같은 날짜에 기존에 저장된 사진을 포함하여 최대 5장까지 저장 가능

                    같은 날짜에 사진이 이미 5장 있으면 여섯 번째 사진은 저장하지 않고
                    `HISTORY_PHOTO409`를 반환합니다. 미래 날짜에는 사진을 추가할 수 없습니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "COMMON200 - 히스토리 사진 추가 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(
                            name = "COMMON200",
                            value = PHOTO_CREATE_SUCCESS_EXAMPLE
                    ))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            COMMON402 - date 또는 image multipart 파트 누락
                            HISTORY400 - 날짜 형식 오류 또는 미래 날짜
                            IMAGE400 - 빈 파일 또는 지원하지 않는 이미지 형식
                            """,
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "HISTORY400", value = HISTORY400_EXAMPLE),
                            @ExampleObject(name = "IMAGE400", value = IMAGE400_EXAMPLE)
                    })
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = """
                            COMMON401 - 인증 정보 없음
                            AUTH006 - 유효하지 않거나 만료된 Access Token
                            AUTH010 - 토큰의 사용자를 찾을 수 없음
                            """,
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON401", value = COMMON401_EXAMPLE),
                            @ExampleObject(name = "AUTH006", value = AUTH006_EXAMPLE),
                            @ExampleObject(name = "AUTH010", value = AUTH010_EXAMPLE)
                    })
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = """
                            AUTH009 - 회원 역할이 아닌 사용자
                            AUTH020 - 비활성 또는 탈퇴 사용자
                            AUTH027 - 게스트 사용자 접근
                            """,
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH009", value = AUTH009_EXAMPLE),
                            @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE),
                            @ExampleObject(name = "AUTH027", value = AUTH027_EXAMPLE)
                    })
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "HISTORY_PHOTO409 - 기존 저장 사진을 포함해 사용자·날짜별 5장에 "
                            + "도달한 상태에서 추가 업로드",
                    content = @Content(examples = @ExampleObject(
                            name = "HISTORY_PHOTO409",
                            value = HISTORY_PHOTO409_EXAMPLE
                    ))
            ),
            @ApiResponse(
                    responseCode = "413",
                    description = "IMAGE413 - 업로드 이미지가 파일당 5MB 초과",
                    content = @Content(examples = @ExampleObject(
                            name = "IMAGE413",
                            value = IMAGE413_EXAMPLE
                    ))
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = """
                            AUTH028 - 인증 저장소 일시 장애
                            HISTORY_PHOTO503 - 사진 저장소 일시 장애
                            """,
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE),
                            @ExampleObject(name = "HISTORY_PHOTO503", value = HISTORY_PHOTO503_EXAMPLE)
                    })
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(
                            name = "COMMON500",
                            value = COMMON500_EXAMPLE
                    ))
            )
    })
    BaseResponse<HistoryPhotoResponse> addHistoryPhoto(
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(
                    description = "multipart/form-data의 사진 기록 날짜(yyyy-MM-dd, 미래 날짜 불가)",
                    required = true,
                    schema = @Schema(type = "string", format = "date"),
                    example = "2026-07-05"
            )
            String date,

            @Parameter(
                    description = "JPG, PNG 또는 WEBP 형식의 이미지 한 장(파일당 최대 5MB)",
                    required = true,
                    schema = @Schema(type = "string", format = "binary")
            )
            MultipartFile image
    );

    @Operation(
            operationId = "deleteHistoryPhoto",
            summary = "날짜별 히스토리 사진 삭제",
            description = "선택 날짜에 속한 본인 사진 한 건을 삭제합니다. 사진 메타데이터 삭제 후 저장소 객체 "
                    + "삭제가 실패해도 API는 성공하고 서버 오류 로그를 남깁니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 히스토리 사진 삭제 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "COMMON200", value = VOID_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "HISTORY400 - 날짜 형식 오류 또는 0 이하의 사진 ID",
                    content = @Content(examples = @ExampleObject(name = "HISTORY400", value = HISTORY400_EXAMPLE))),
            @ApiResponse(responseCode = "401",
                    description = "COMMON401/AUTH006/AUTH010 - 인증 토큰 또는 사용자 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON401", value = COMMON401_EXAMPLE),
                            @ExampleObject(name = "AUTH006", value = AUTH006_EXAMPLE),
                            @ExampleObject(name = "AUTH010", value = AUTH010_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "403",
                    description = "AUTH009/AUTH020/AUTH027 - 회원 권한 또는 사용자 상태 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH009", value = AUTH009_EXAMPLE),
                            @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE),
                            @ExampleObject(name = "AUTH027", value = AUTH027_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "404", description = "HISTORY_PHOTO404 - 사진 없음, 날짜 불일치 또는 다른 회원 소유",
                    content = @Content(examples = @ExampleObject(
                            name = "HISTORY_PHOTO404",
                            value = HISTORY_PHOTO404_EXAMPLE
                    ))),
            @ApiResponse(responseCode = "503", description = "AUTH028 - 인증 저장소 일시 장애",
                    content = @Content(examples = @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    BaseResponse<Void> deleteHistoryPhoto(
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(description = "사진 기록 날짜(yyyy-MM-dd)", example = "2026-07-05") String date,
            @Parameter(description = "삭제할 사진 ID(양수)", example = "3") Long photoId
    );
}
