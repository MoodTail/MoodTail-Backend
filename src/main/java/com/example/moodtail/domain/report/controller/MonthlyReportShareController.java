package com.example.moodtail.domain.report.controller;

import com.example.moodtail.domain.report.controller.docs.MonthlyReportShareControllerDocs;
import com.example.moodtail.domain.report.dto.response.MonthlyReportSharedImageResponse;
import com.example.moodtail.domain.report.service.MonthlyReportShareImageService;
import com.example.moodtail.global.common.base.BaseResponse;
import lombok.RequiredArgsConstructor;
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
}
