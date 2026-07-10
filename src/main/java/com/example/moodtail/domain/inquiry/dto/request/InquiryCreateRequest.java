package com.example.moodtail.domain.inquiry.dto.request;

public record InquiryCreateRequest(
        String inquiryType,
        String content,
        String contactEmail
) {
}
