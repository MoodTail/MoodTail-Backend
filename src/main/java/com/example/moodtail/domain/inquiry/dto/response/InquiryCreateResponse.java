package com.example.moodtail.domain.inquiry.dto.response;

import com.example.moodtail.domain.inquiry.entity.Inquiry;
import com.example.moodtail.domain.inquiry.entity.InquiryStatus;

import java.time.LocalDateTime;

public record InquiryCreateResponse(
        Long inquiryId,
        InquiryStatus status,
        LocalDateTime createdAt
) {

    public static InquiryCreateResponse from(Inquiry inquiry) {
        return new InquiryCreateResponse(
                inquiry.getId(),
                inquiry.getStatus(),
                inquiry.getCreatedAt()
        );
    }
}
