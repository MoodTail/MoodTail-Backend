package com.example.moodtail.domain.moodtest.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag(name = "Mood Test Shares", description = "테스트 결과 공유 URL 생성 및 공개 결과 조회 API")
public interface MoodTestResultSharePageControllerDocs {

    String SUCCESS_EXAMPLE = """
            <!doctype html>
            <html lang="ko">
            <head>
              <meta charset="UTF-8">
              <meta property="og:title" content="MoodTail 테스트 결과">
              <meta property="og:description" content="나의 무드 타입과 추천 칵테일을 확인해보세요.">
              <meta property="og:image" content="https://cdn.moodtail.com/public/share/test-results/result.png">
              <meta property="og:url" content="https://api.moodtail.com/share/results/r_hJ7JngQmYV4x0aP9k2LmN3Qr">
              <meta http-equiv="refresh" content="0;url=https://moodtail.com/share/results/r_hJ7JngQmYV4x0aP9k2LmN3Qr">
            </head>
            <body></body>
            </html>
            """;
    String MOOD_TEST404_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"MOOD_TEST404","message":"공유된 테스트 결과를 찾을 수 없습니다."}
            """;
    String COMMON500_EXAMPLE = """
            {"timestamp":"2026-07-21T14:30:00","code":"COMMON500","message":"서버 에러가 발생했습니다."}
            """;

    @Operation(operationId = "getMoodTestResultSharePage", summary = "공유 결과 OG 메타데이터 페이지 조회",
            description = "SNS 크롤러에는 저장된 썸네일의 OG 메타데이터를 제공하고 일반 브라우저는 프론트엔드 결과 페이지로 이동시킵니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OG 메타데이터가 포함된 UTF-8 HTML",
                    content = @Content(mediaType = MediaType.TEXT_HTML_VALUE, schema = @Schema(type = "string"),
                            examples = @ExampleObject(name = "성공 HTML", value = SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "MOOD_TEST404 - 공유 토큰에 해당하는 결과가 없음",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "MOOD_TEST404", value = MOOD_TEST404_EXAMPLE))),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)))
    })
    ResponseEntity<String> getSharePage(
            @Parameter(description = "공유 결과 토큰", example = "r_hJ7JngQmYV4x0aP9k2LmN3Qr") String shareToken
    );
}
