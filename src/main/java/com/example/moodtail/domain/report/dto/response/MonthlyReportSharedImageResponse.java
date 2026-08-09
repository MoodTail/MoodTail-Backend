package com.example.moodtail.domain.report.dto.response;

public record MonthlyReportSharedImageResponse(
        int year,
        int month,
        String shareImageUrl
) {
}
