package com.example.moodtail.domain.report.dto.response;

public record MonthlyReportSharedImageFile(
        byte[] content,
        String contentType
) {
}
