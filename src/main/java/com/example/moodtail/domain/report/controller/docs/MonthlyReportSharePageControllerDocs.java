package com.example.moodtail.domain.report.controller.docs;

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

@Tag(name = "Reports", description = "월간 리포트 공개 공유 API")
public interface MonthlyReportSharePageControllerDocs {

    String SUCCESS_EXAMPLE = """
            <!doctype html>
            <html lang="ko">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>MoodTail 월간 리포트</title>
                <meta name="description" content="한 달 동안 쌓인 나의 무드와 칵테일 기록을 확인해 보세요.">
                <meta property="og:type" content="website">
                <meta property="og:title" content="MoodTail 월간 리포트">
                <meta property="og:description" content="한 달 동안 쌓인 나의 무드와 칵테일 기록을 확인해 보세요.">
                <meta property="og:image" content="https://moodtail-bucket.s3.ap-southeast-2.amazonaws.com/public/reports/monthly/8d5f57e1-40e5-46b2-852d-1c3dd640efb8.png">
                <meta property="og:url" content="https://mood-tail.site/share/reports/monthly/mr_hJ7JngQmYV4x0aP9k2LmN3Qr">
                <meta http-equiv="refresh" content="0;url=https://mood-tail.site/reports/monthly/share/mr_hJ7JngQmYV4x0aP9k2LmN3Qr">
                <link rel="canonical" href="https://mood-tail.site/share/reports/monthly/mr_hJ7JngQmYV4x0aP9k2LmN3Qr">
            </head>
            <body>
                <p><a href="https://mood-tail.site/reports/monthly/share/mr_hJ7JngQmYV4x0aP9k2LmN3Qr">MoodTail 월간 리포트 확인하기</a></p>
            </body>
            </html>
            """;
    String REPORT404_EXAMPLE = """
            {
              "timestamp": "2026-08-09T14:30:00",
              "code": "REPORT404",
              "message": "공유된 월간 리포트를 찾을 수 없습니다."
            }
            """;
    String COMMON500_EXAMPLE = """
            {
              "timestamp": "2026-08-09T14:30:00",
              "code": "COMMON500",
              "message": "서버 에러가 발생했습니다."
            }
            """;

    @Operation(
            operationId = "getMonthlyReportSharePage",
            summary = "월간 리포트 공유 OG 페이지 조회",
            description = "SNS 크롤러에는 공유 이미지의 OG 메타데이터를 제공하고 일반 브라우저는 "
                    + "프론트엔드 월간 리포트 공유 화면으로 이동시킵니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OG 메타데이터가 포함된 UTF-8 HTML",
                    content = @Content(
                            mediaType = MediaType.TEXT_HTML_VALUE,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(
                                    name = "성공 HTML",
                                    value = SUCCESS_EXAMPLE
                            )
                    )),
            @ApiResponse(responseCode = "404", description = "REPORT404 - 토큰이 없거나 공유 기간이 만료됨",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "REPORT404", value = REPORT404_EXAMPLE)
                    )),
            @ApiResponse(responseCode = "500", description = "COMMON500 - 서버 내부 오류",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "COMMON500", value = COMMON500_EXAMPLE)
                    ))
    })
    ResponseEntity<String> getSharePage(
            @Parameter(
                    description = "월간 리포트 공유 토큰",
                    example = "mr_hJ7JngQmYV4x0aP9k2LmN3Qr"
            )
            String shareToken
    );
}
