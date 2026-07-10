package com.example.moodtail.domain.inquiry.controller;

import com.example.moodtail.domain.inquiry.dto.request.InquiryCreateRequest;
import com.example.moodtail.domain.inquiry.dto.response.InquiryCreateResponse;
import com.example.moodtail.domain.inquiry.service.InquiryService;
import com.example.moodtail.global.common.base.BaseResponse;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inquiries")
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping
    public BaseResponse<InquiryCreateResponse> createInquiry(
            @RequestBody InquiryCreateRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        return BaseResponse.onSuccess(inquiryService.createInquiry(request, principalDetails));
    }
}
