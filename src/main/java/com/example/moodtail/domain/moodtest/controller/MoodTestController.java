package com.example.moodtail.domain.moodtest.controller;

import com.example.moodtail.domain.moodtest.dto.response.MoodTestQuestionResponse;
import com.example.moodtail.domain.moodtest.service.MoodTestQuestionService;
import com.example.moodtail.global.common.base.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tests")
public class MoodTestController {

    private final MoodTestQuestionService moodTestQuestionService;

    @GetMapping("/questions")
    public BaseResponse<MoodTestQuestionResponse> getQuestions() {
        return BaseResponse.onSuccess(moodTestQuestionService.getQuestions());
    }
}
