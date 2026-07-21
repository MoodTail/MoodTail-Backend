package com.example.moodtail.domain.moodtest.controller;

import com.example.moodtail.domain.moodtest.controller.docs.MoodTestResultSaveControllerDocs;
import com.example.moodtail.domain.moodtest.dto.request.MoodTestResultSaveRequest;
import com.example.moodtail.domain.moodtest.dto.response.MoodTestResultSaveApiResponse;
import com.example.moodtail.domain.moodtest.service.MoodTestResultSaveService;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tests/results")
public class MoodTestResultSaveController implements MoodTestResultSaveControllerDocs {

    private final MoodTestResultSaveService moodTestResultSaveService;

    @PostMapping("/save")
    public MoodTestResultSaveApiResponse saveResult(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody MoodTestResultSaveRequest request
    ) {
        Long testResultId = moodTestResultSaveService.saveResult(principalDetails.getUserId(), request);
        return MoodTestResultSaveApiResponse.success(testResultId);
    }
}
