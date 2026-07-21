package com.example.moodtail.domain.auth.model;

public record AuthResult<T>(
        T response,
        String refreshToken
) {
}
