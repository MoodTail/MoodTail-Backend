package com.example.moodtail.domain.moodtest.controller;

import com.example.moodtail.domain.moodtest.dto.response.MoodTestResultSharePageResponse;
import com.example.moodtail.domain.moodtest.service.MoodTestResultShareService;
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
@RequestMapping("/share/results")
public class MoodTestResultSharePageController {

    private static final String TITLE = "MoodTail 무드 테스트 결과";
    private static final String DESCRIPTION = "나의 취향에 어울리는 무드 타입과 칵테일을 확인해 보세요.";
    private static final MediaType HTML_UTF8 =
            new MediaType("text", "html", StandardCharsets.UTF_8);

    private final MoodTestResultShareService moodTestResultShareService;

    @GetMapping(value = "/{shareToken}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getSharePage(@PathVariable String shareToken) {
        MoodTestResultSharePageResponse page =
                moodTestResultShareService.getSharePage(shareToken);

        return ResponseEntity.ok()
                .contentType(HTML_UTF8)
                .body(createHtml(page));
    }

    private String createHtml(MoodTestResultSharePageResponse page) {
        String title = HtmlUtils.htmlEscape(TITLE);
        String description = HtmlUtils.htmlEscape(DESCRIPTION);
        String shareUrl = HtmlUtils.htmlEscape(page.shareUrl());
        String frontendUrl = HtmlUtils.htmlEscape(page.frontendUrl());
        String thumbnailImageUrl = HtmlUtils.htmlEscape(page.thumbnailImageUrl());

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
                    <p><a href="%s">MoodTail 테스트 결과 확인하기</a></p>
                </body>
                </html>
                """.formatted(
                title,
                description,
                title,
                description,
                thumbnailImageUrl,
                shareUrl,
                frontendUrl,
                shareUrl,
                frontendUrl
        );
    }
}
