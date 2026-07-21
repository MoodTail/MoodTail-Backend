package com.example.moodtail.domain.term.controller;

import com.example.moodtail.domain.term.controller.docs.TermControllerDocs;
import com.example.moodtail.domain.term.dto.response.TermsResponse;
import com.example.moodtail.domain.term.service.TermService;
import com.example.moodtail.global.common.base.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/terms")
public class TermController implements TermControllerDocs {

    private final TermService termService;

    @GetMapping
    public BaseResponse<TermsResponse> getTerms(
            @RequestParam(required = false) String termType
    ) {
        return BaseResponse.onSuccess(termService.getTerms(termType));
    }
}
