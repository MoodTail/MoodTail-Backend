package com.example.moodtail.domain.report.controller.docs;

import com.example.moodtail.domain.report.dto.response.MonthlyReportResponse;
import com.example.moodtail.domain.report.dto.response.MonthlyReportShareImageResponse;
import com.example.moodtail.domain.report.dto.response.MonthlyReportSharedImageResponse;
import com.example.moodtail.global.common.base.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;

/** Concrete generic wrappers used only to make Springdoc response schemas deterministic. */
public final class ReportApiResponseSchemas {

    private ReportApiResponseSchemas() {
    }

    @Schema(name = "MonthlyReportApiResponse")
    public static final class MonthlyReport extends BaseResponse<MonthlyReportResponse> {
        private MonthlyReport() {
            super(null, null, null);
        }
    }

    @Schema(name = "MonthlyReportShareImageApiResponse")
    public static final class ShareImage extends BaseResponse<MonthlyReportShareImageResponse> {
        private ShareImage() {
            super(null, null, null);
        }
    }

    @Schema(name = "MonthlyReportSharedImageApiResponse")
    public static final class SharedImage extends BaseResponse<MonthlyReportSharedImageResponse> {
        private SharedImage() {
            super(null, null, null);
        }
    }
}
