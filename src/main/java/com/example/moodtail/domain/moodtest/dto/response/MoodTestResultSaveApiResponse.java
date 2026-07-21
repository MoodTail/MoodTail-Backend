package com.example.moodtail.domain.moodtest.dto.response;

public record MoodTestResultSaveApiResponse(
        boolean isSuccess,
        String code,
        String message,
        MoodTestResultSaveResponse result
) {

    public static MoodTestResultSaveApiResponse success(Long testResultId) {
        return new MoodTestResultSaveApiResponse(
                true,
                "200",
                "테스트 분석 결과 저장 성공",
                new MoodTestResultSaveResponse(testResultId)
        );
    }
}
