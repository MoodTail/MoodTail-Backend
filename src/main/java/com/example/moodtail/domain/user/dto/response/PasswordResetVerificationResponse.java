package com.example.moodtail.domain.user.dto.response;

public record PasswordResetVerificationResponse(
        String resetToken,
        long expiresInSeconds
) {
}
