package com.example.moodtail.domain.user.dto.response;

public record UserProfileUpdateResponse(
        Long userId,
        String nickname
) {
}
