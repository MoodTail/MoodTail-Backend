package com.example.moodtail.domain.report.controller;

import com.example.moodtail.domain.report.controller.docs.MonthlyReportControllerDocs;
import com.example.moodtail.domain.report.dto.response.MonthlyReportResponse;
import com.example.moodtail.domain.report.dto.response.MonthlyReportShareImageResponse;
import com.example.moodtail.domain.report.service.MonthlyReportService;
import com.example.moodtail.domain.report.service.MonthlyReportShareImageService;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
public class MonthlyReportController implements MonthlyReportControllerDocs {

    private final MonthlyReportService monthlyReportService;
    private final MonthlyReportShareImageService shareImageService;

    @Override
    @GetMapping("/monthly")
    public BaseResponse<MonthlyReportResponse> getMonthlyReport(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return BaseResponse.onSuccess(
                monthlyReportService.getMonthlyReport(principal.getUserId(), year, month)
        );
    }

    @Override
    @PostMapping(
            path = "/monthly/share-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public BaseResponse<MonthlyReportShareImageResponse> createMonthlyReportShareImage(
            @AuthenticationPrincipal PrincipalDetails principal,
            @RequestParam int year,
            @RequestParam int month,
            @RequestPart("image") MultipartFile image
    ) {
        return BaseResponse.onSuccess(
                shareImageService.uploadShareImage(principal.getUserId(), year, month, image)
        );
    }
}
