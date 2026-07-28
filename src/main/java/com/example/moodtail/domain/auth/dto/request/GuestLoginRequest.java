package com.example.moodtail.domain.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GuestLoginRequest(
        @Schema(
                description = "기기에서 생성해 보관하는 게스트 식별 UUID. "
                        + "uuid라는 필드명도 허용합니다.",
                format = "uuid",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        @JsonAlias("uuid")
        @NotNull(message = "게스트 UUID는 필수입니다.")
        UUID guestUuid
) {
}
