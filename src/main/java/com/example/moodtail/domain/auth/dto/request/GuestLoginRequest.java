package com.example.moodtail.domain.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GuestLoginRequest(
        @JsonAlias("uuid")
        @NotNull(message = "게스트 UUID는 필수입니다.")
        UUID guestUuid
) {
}
