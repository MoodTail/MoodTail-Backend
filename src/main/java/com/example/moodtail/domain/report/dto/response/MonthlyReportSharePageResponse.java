package com.example.moodtail.domain.report.dto.response;

public record MonthlyReportSharePageResponse(
        String shareUrl,
        String frontendUrl,
        String shareImageUrl
) {
}
