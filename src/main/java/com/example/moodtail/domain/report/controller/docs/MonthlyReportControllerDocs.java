package com.example.moodtail.domain.report.controller.docs;

import com.example.moodtail.domain.report.dto.response.MonthlyReportResponse;
import com.example.moodtail.domain.report.dto.response.MonthlyReportShareImageResponse;
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
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Reports", description = "회원의 월간 리포트 조회 및 공유 이미지 API")
@SecurityRequirement(name = "bearerAuth")
public interface MonthlyReportControllerDocs {

    String MONTHLY_REPORT_SUCCESS_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"COMMON200","message":"요청에 성공했습니다.","result":{"year":2026,"month":7,"monthlyMoodType":{"moodTypeId":2001,"typeCode":"TYPE01","name":"몽글몽글 낭만파","shortDescription":"부드러운 달콤함 속에서 여유를 즐기는 타입","characterImageUrl":"https://cdn.moodtail.com/mood-types/type01.png"},"topMoodTypes":[{"moodTypeId":2001,"typeCode":"TYPE01","name":"몽글몽글 낭만파","characterImageUrl":"https://cdn.moodtail.com/mood-types/type01.png","count":2,"ranking":1},{"moodTypeId":2002,"typeCode":"TYPE02","name":"반짝이는 모험가","characterImageUrl":"https://cdn.moodtail.com/mood-types/type02.png","count":2,"ranking":1},{"moodTypeId":2003,"typeCode":"TYPE03","name":"차분한 사색가","characterImageUrl":"https://cdn.moodtail.com/mood-types/type03.png","count":1,"ranking":3}],"averageTasteProfile":{"alcoholIntensity":2.8,"sweetness":4.1,"sourness":2.5,"refreshing":3.8,"bitterness":1.7},"displayAverageTasteScores":{"alcoholIntensity":45,"sweetness":78,"sourness":38,"refreshing":70,"bitterness":18},"previousMonthTasteProfile":null,"previousMonthDisplayTasteScores":null,"frequentCocktails":[{"cocktailId":10,"nameKo":"모히또","nameEn":"Mojito","shortDescription":"상쾌한 민트와 라임의 조화","imageUrl":"https://cdn.moodtail.com/cocktails/mojito.png","count":2,"ranking":1},{"cocktailId":11,"nameKo":"보드카 토닉","nameEn":"Vodka Tonic","shortDescription":"깔끔한 보드카와 토닉워터의 조화","imageUrl":"https://cdn.moodtail.com/cocktails/vodka-tonic.png","count":2,"ranking":1},{"cocktailId":12,"nameKo":"진 토닉","nameEn":"Gin Tonic","shortDescription":"향긋한 진과 토닉워터의 조화","imageUrl":"https://cdn.moodtail.com/cocktails/gin-tonic.png","count":1,"ranking":3}],"activity":{"testCount":5,"drinkingRecordCount":5}}}
            """;
    String SHARE_IMAGE_SUCCESS_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"COMMON200","message":"요청에 성공했습니다.","result":{"shareImageUrl":"https://moodtail.s3.ap-northeast-2.amazonaws.com/reports/monthly/share-image.png"}}
            """;
    String COMMON401_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"COMMON401","message":"인증이 필요합니다."}
            """;
    String COMMON402_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"COMMON402","message":"입력값 검증에 실패했습니다."}
            """;
    String COMMON405_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"COMMON405","message":"요청 인자 타입이 올바르지 않습니다."}
            """;
    String COMMON500_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"COMMON500","message":"서버 에러가 발생했습니다."}
            """;
    String AUTH006_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH006","message":"유효하지 않은 액세스 토큰입니다."}
            """;
    String AUTH009_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH009","message":"권한이 없습니다."}
            """;
    String AUTH010_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH010","message":"존재하지 않는 사용자입니다."}
            """;
    String AUTH020_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH020","message":"비활성화된 사용자입니다."}
            """;
    String AUTH027_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH027","message":"기록을 저장하려면 로그인하세요"}
            """;
    String AUTH028_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"AUTH028","message":"인증 서비스를 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도해주세요."}
            """;
    String REPORT_400_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"REPORT_400","message":"월간 리포트 요청 값이 올바르지 않습니다."}
            """;
    String REPORT_409_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"REPORT_409","message":"월간 리포트 생성에 필요한 데이터가 부족합니다."}
            """;
    String REPORT_IMAGE_503_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"REPORT_IMAGE_503","message":"월간 리포트 공유 이미지를 일시적으로 저장할 수 없습니다."}
            """;
    String IMAGE400_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"IMAGE400","message":"이미지 파일 형식이 올바르지 않습니다."}
            """;
    String IMAGE413_EXAMPLE = """
            {"timestamp":"2026-07-24T14:30:00","code":"IMAGE413","message":"이미지 파일은 5MB 이하여야 합니다."}
            """;

    @Operation(
            operationId = "getMonthlyReport",
            summary = "월간 리포트 조회",
            description = "지정 월의 테스트 결과와 음주 기록을 집계합니다. 해당 월 테스트 결과가 5건 "
                    + "이상이어야 하며 미래 월은 조회할 수 없습니다. topMoodTypes는 1~3개, "
                    + "frequentCocktails는 0~3개이고 공동 순위는 1, 1, 3처럼 반환될 수 있습니다. "
                    + "맛 원본 점수는 1.0~5.0, display 점수는 0~100입니다. 이전 달 테스트 결과가 없으면 "
                    + "previousMonthTasteProfile과 previousMonthDisplayTasteScores는 null입니다. "
                    + "이미지 URL도 등록 상태에 따라 null일 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 월간 리포트 조회 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(
                            name = "COMMON200",
                            value = MONTHLY_REPORT_SUCCESS_EXAMPLE
                    ))),
            @ApiResponse(responseCode = "400",
                    description = "COMMON402/COMMON405/REPORT_400 - 누락·타입 오류 또는 유효하지 않은 연도·월",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "COMMON405", value = COMMON405_EXAMPLE),
                            @ExampleObject(name = "REPORT_400", value = REPORT_400_EXAMPLE)
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
            @ApiResponse(responseCode = "409", description = "REPORT_409 - 해당 월 테스트 결과 5건 미만",
                    content = @Content(examples = @ExampleObject(name = "REPORT_409", value = REPORT_409_EXAMPLE))),
            @ApiResponse(responseCode = "503", description = "AUTH028 - 인증 저장소 일시 장애",
                    content = @Content(examples = @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    BaseResponse<MonthlyReportResponse> getMonthlyReport(
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(description = "조회 연도(1000 이상, 미래 월 조회 불가)", example = "2026") int year,
            @Parameter(description = "조회 월(1~12)", example = "7") int month
    );

    @Operation(
            operationId = "createMonthlyReportShareImage",
            summary = "월간 리포트 공유 이미지 저장",
            description = "프론트엔드에서 월간 리포트 데이터로 생성한 JPG, PNG 또는 WEBP 이미지를 S3에 "
                    + "저장하고 접근 URL을 반환합니다. 파일당 최대 5MB입니다. 조회 API와 동일하게 해당 월 "
                    + "테스트 결과가 5건 이상이어야 합니다. 공유 이미지는 응답 시점부터 정확히 30일이 아니라, "
                    + "객체 생성 30일 경과 후 S3 수명 주기 정책이 실행되는 시점에 삭제됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "COMMON200 - 월간 리포트 공유 이미지 저장 성공",
                    useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(
                            name = "COMMON200",
                            value = SHARE_IMAGE_SUCCESS_EXAMPLE
                    ))),
            @ApiResponse(responseCode = "400",
                    description = "COMMON402/COMMON405/REPORT_400/IMAGE400 - 파라미터·날짜 또는 이미지 형식 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "COMMON405", value = COMMON405_EXAMPLE),
                            @ExampleObject(name = "REPORT_400", value = REPORT_400_EXAMPLE),
                            @ExampleObject(name = "IMAGE400", value = IMAGE400_EXAMPLE)
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
            @ApiResponse(responseCode = "409", description = "REPORT_409 - 해당 월 테스트 결과 5건 미만",
                    content = @Content(examples = @ExampleObject(name = "REPORT_409", value = REPORT_409_EXAMPLE))),
            @ApiResponse(responseCode = "413", description = "IMAGE413 - 이미지 파일 5MB 초과",
                    content = @Content(examples = @ExampleObject(name = "IMAGE413", value = IMAGE413_EXAMPLE))),
            @ApiResponse(responseCode = "503",
                    description = "AUTH028/REPORT_IMAGE_503 - 인증 저장소 또는 공유 이미지 저장소 장애",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH028", value = AUTH028_EXAMPLE),
                            @ExampleObject(name = "REPORT_IMAGE_503", value = REPORT_IMAGE_503_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    BaseResponse<MonthlyReportShareImageResponse> createMonthlyReportShareImage(
            @Parameter(hidden = true) PrincipalDetails principal,
            @Parameter(description = "리포트 연도(1000 이상, 미래 월 불가)", example = "2026") int year,
            @Parameter(description = "리포트 월(1~12)", example = "7") int month,
            @Parameter(description = "프론트엔드에서 생성한 JPG, PNG 또는 WEBP 이미지(최대 5MB)")
            MultipartFile image
    );
}
