package com.example.moodtail.domain.report.controller;

import com.example.moodtail.domain.report.controller.docs.MonthlyReportShareControllerDocs;
import com.example.moodtail.domain.report.dto.response.MonthlyReportSharedImageFile;
import com.example.moodtail.domain.report.dto.response.MonthlyReportSharedImageResponse;
import com.example.moodtail.domain.report.service.MonthlyReportShareImageService;
import com.example.moodtail.global.common.base.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports/monthly/shares")
public class MonthlyReportShareController implements MonthlyReportShareControllerDocs {

    private final MonthlyReportShareImageService shareImageService;

    @Override
    @GetMapping("/{shareToken}")
    public BaseResponse<MonthlyReportSharedImageResponse> getSharedImage(
            @PathVariable String shareToken
    ) {
        return BaseResponse.onSuccess(shareImageService.getSharedImage(shareToken));
    }

    @Override
    @GetMapping("/{shareToken}/image")
    public ResponseEntity<byte[]> getSharedImageFile(@PathVariable String shareToken) {
        MonthlyReportSharedImageFile image = shareImageService.getSharedImageFile(shareToken);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .contentType(MediaType.parseMediaType(image.contentType()))
                .contentLength(image.content().length)
                .body(image.content());
    }
}
