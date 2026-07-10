package com.example.moodtail.domain.user.dto.request;

public record UserProfileUpdateRequest(
        String nickname,
        Long representativeMoodTypeId
) {
}
