package com.example.moodtail.domain.moodtest.controller.docs;

import com.example.moodtail.domain.moodtest.dto.request.MoodTestResultSaveRequest;
import com.example.moodtail.domain.moodtest.dto.response.MoodTestResultSaveApiResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Mood Tests", description = "무드 테스트 문항 조회 및 결과 산출 API")
@SecurityRequirement(name = "bearerAuth")
public interface MoodTestResultSaveControllerDocs {

    String SUCCESS_EXAMPLE = """
            {"isSuccess":true,"code":"200","message":"테스트 분석 결과 저장 성공","result":{"test_result_id":10}}
            """;
    String COMMON402_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"COMMON402","message":"입력값 검증에 실패했습니다."}
            """;
    String MOOD_TEST400_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"MOOD_TEST400","message":"테스트 결과 저장 요청이 올바르지 않습니다."}
            """;
    String COMMON401_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"COMMON401","message":"인증이 필요합니다."}
            """;
    String AUTH002_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"AUTH002","message":"만료된 JWT입니다."}
            """;
    String AUTH006_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"AUTH006","message":"유효하지 않은 액세스 토큰입니다."}
            """;
    String AUTH010_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"AUTH010","message":"존재하지 않는 사용자입니다."}
            """;
    String AUTH009_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"AUTH009","message":"권한이 없습니다."}
            """;
    String AUTH020_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"AUTH020","message":"비활성화된 사용자입니다."}
            """;
    String MOOD_TEST404_MOOD_TYPE_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"MOOD_TEST404","message":"무드 타입을 찾을 수 없습니다."}
            """;
    String MOOD_TEST404_COCKTAIL_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"MOOD_TEST404","message":"추천 칵테일을 찾을 수 없습니다."}
            """;
    String COMMON500_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"COMMON500","message":"서버 에러가 발생했습니다."}
            """;

    @Operation(operationId = "saveMoodTestResult", summary = "테스트 분석 결과 저장",
            description = "로그인 사용자의 테스트 결과와 추천 칵테일 4종을 저장합니다. "
                    + "같은 날짜의 결과가 있으면 갱신하고 해당 무드 타입을 해금합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "응답 본문 code=200 - 테스트 분석 결과 저장 성공", useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "성공", value = SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "COMMON402 - 요청값 검증 실패\nMOOD_TEST400 - 저장 요청의 타입·점수·추천 목록 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON402", value = COMMON402_EXAMPLE),
                            @ExampleObject(name = "MOOD_TEST400", value = MOOD_TEST400_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "401", description = "COMMON401/AUTH002/AUTH006/AUTH010 - 인증 토큰 또는 사용자 오류",
                    content = @Content(examples = {
                            @ExampleObject(name = "COMMON401", value = COMMON401_EXAMPLE),
                            @ExampleObject(name = "AUTH002", value = AUTH002_EXAMPLE),
                            @ExampleObject(name = "AUTH006", value = AUTH006_EXAMPLE),
                            @ExampleObject(name = "AUTH010", value = AUTH010_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "403", description = "AUTH009/AUTH020 - 역할 불일치 또는 비활성 사용자",
                    content = @Content(examples = {
                            @ExampleObject(name = "AUTH009", value = AUTH009_EXAMPLE),
                            @ExampleObject(name = "AUTH020", value = AUTH020_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "404", description = "MOOD_TEST404 - 무드 타입 또는 추천 칵테일을 찾을 수 없음",
                    content = @Content(examples = {
                            @ExampleObject(name = "무드 타입 없음", value = MOOD_TEST404_MOOD_TYPE_EXAMPLE),
                            @ExampleObject(name = "추천 칵테일 없음", value = MOOD_TEST404_COCKTAIL_EXAMPLE)
                    })),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    MoodTestResultSaveApiResponse saveResult(
            @Parameter(hidden = true) PrincipalDetails principalDetails,
            MoodTestResultSaveRequest request
    );
}
