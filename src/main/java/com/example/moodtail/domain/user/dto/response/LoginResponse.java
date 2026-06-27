package com.example.moodtail.domain.user.dto.response;

public record LoginResponse(
        Long userId,
        String email,
        String nickname,
        String accessToken
) {
}
