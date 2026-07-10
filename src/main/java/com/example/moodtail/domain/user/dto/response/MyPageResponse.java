package com.example.moodtail.domain.user.dto.response;

public record MyPageResponse(
        Long userId,
        String nickname,
        RepresentativeMoodTypeResponse representativeMoodType,
        long totalTestCount,
        long monthlyRecordCount,
        long unlockedMoodTypeCount
) {

    public record RepresentativeMoodTypeResponse(
            Long moodTypeId,
            String typeCode,
            String name,
            String characterImageUrl
    ) {
    }
}
