package com.example.moodtail.domain.report.controller;

import com.example.moodtail.domain.report.controller.docs.MonthlyReportSharePageControllerDocs;
import com.example.moodtail.domain.report.dto.response.MonthlyReportSharePageResponse;
import com.example.moodtail.domain.report.service.MonthlyReportShareImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/share/reports/monthly")
public class MonthlyReportSharePageController implements MonthlyReportSharePageControllerDocs {

    private static final String TITLE = "MoodTail 월간 리포트";
    private static final String DESCRIPTION = "한 달 동안 쌓인 나의 무드와 칵테일 기록을 확인해 보세요.";
    private static final MediaType HTML_UTF8 =
            new MediaType("text", "html", StandardCharsets.UTF_8);

    private final MonthlyReportShareImageService shareImageService;

    @Override
    @GetMapping(value = "/{shareToken}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getSharePage(@PathVariable String shareToken) {
        MonthlyReportSharePageResponse page = shareImageService.getSharePage(shareToken);

        return ResponseEntity.ok()
                .contentType(HTML_UTF8)
                .body(createHtml(page));
    }

    private String createHtml(MonthlyReportSharePageResponse page) {
        String title = HtmlUtils.htmlEscape(TITLE);
        String description = HtmlUtils.htmlEscape(DESCRIPTION);
        String shareUrl = HtmlUtils.htmlEscape(page.shareUrl());
        String frontendUrl = HtmlUtils.htmlEscape(page.frontendUrl());
        String shareImageUrl = HtmlUtils.htmlEscape(page.shareImageUrl());

        return """
                <!doctype html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>%s</title>
                    <meta name="description" content="%s">
                    <meta property="og:type" content="website">
                    <meta property="og:title" content="%s">
                    <meta property="og:description" content="%s">
                    <meta property="og:image" content="%s">
                    <meta property="og:url" content="%s">
                    <meta http-equiv="refresh" content="0;url=%s">
                    <link rel="canonical" href="%s">
                </head>
                <body>
                    <p><a href="%s">MoodTail 월간 리포트 확인하기</a></p>
                </body>
                </html>
                """.formatted(
                title,
                description,
                title,
                description,
                shareImageUrl,
                shareUrl,
                frontendUrl,
                shareUrl,
                frontendUrl
        );
    }
}
