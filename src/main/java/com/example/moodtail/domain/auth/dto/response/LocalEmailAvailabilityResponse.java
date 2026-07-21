package com.example.moodtail.domain.auth.dto.response;

public record LocalEmailAvailabilityResponse(
        String email,
        boolean available
) {
}
