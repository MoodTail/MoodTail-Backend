package com.example.moodtail.domain.moodtest.controller;

import com.example.moodtail.domain.moodtest.dto.request.MoodTestResultRequest;
import com.example.moodtail.domain.moodtest.dto.response.MoodTestQuestionResponse;
import com.example.moodtail.domain.moodtest.dto.response.MoodTestResultResponse;
import com.example.moodtail.domain.moodtest.service.MoodTestQuestionService;
import com.example.moodtail.domain.moodtest.service.MoodTestResultService;
import com.example.moodtail.global.common.base.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tests")
public class MoodTestController {

    private final MoodTestQuestionService moodTestQuestionService;
    private final MoodTestResultService moodTestResultService;

    @GetMapping("/questions")
    public BaseResponse<MoodTestQuestionResponse> getQuestions() {
        return BaseResponse.onSuccess(moodTestQuestionService.getQuestions());
    }

    @PostMapping("/results")
    public BaseResponse<MoodTestResultResponse> calculateResult(
            @Valid @RequestBody MoodTestResultRequest request
    ) {
        return BaseResponse.onSuccess(moodTestResultService.calculateResult(request));
    }
}
